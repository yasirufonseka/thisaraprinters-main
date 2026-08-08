package com.example.thisaraprinters.service;

import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    /** Applies optional, report-specific filters after the report data is assembled and refreshes its summary values. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> applyFiltersAndRecalculate(String reportType, Map<String, Object> result,
                                                            Map<String, String> requestParameters) {
        List<Map<String, Object>> rows = new ArrayList<>((List<Map<String, Object>>) result.getOrDefault("rows", List.of()));
        Map<String, String> filters = requestParameters.entrySet().stream()
                .filter(e -> !Set.of("start", "end").contains(e.getKey()) && e.getValue() != null && !e.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        rows = rows.stream().filter(row -> filters.entrySet().stream().allMatch(filter ->
                String.valueOf(row.getOrDefault(filter.getKey(), "")).toLowerCase(Locale.ROOT)
                        .contains(filter.getValue().trim().toLowerCase(Locale.ROOT)))).collect(Collectors.toList());
        result.put("rows", rows);

        switch (reportType) {
            case "sales" -> {
                result.put("totalRevenue", sum(rows, "amount")); result.put("totalAdvance", sum(rows, "advanceAmount"));
                result.put("totalOrders", rows.size()); result.put("pendingCount", count(rows, "status", "Pending"));
                result.put("acceptedCount", rows.stream().filter(r -> value(r, "status").contains("approved")).count());
                result.put("byStatus", countBy(rows, "status"));
            }
            case "inventory" -> { result.put("totalLots", rows.size()); result.put("totalQty", sum(rows, "quantity")); result.put("belowReorderCount", rows.stream().filter(r -> Boolean.TRUE.equals(r.get("belowReorder"))).count()); }
            case "grn" -> { result.put("totalGRNs", rows.size()); result.put("totalQtyReceived", sum(rows, "receivedQty")); }
            case "purchase-orders" -> { result.put("totalOrders", rows.size()); result.put("totalPaid", sum(rows, "paidAmount")); result.put("unpaidCount", count(rows, "paymentStatus", "Unpaid")); result.put("byPaymentStatus", countBy(rows, "paymentStatus")); }
            case "production" -> { result.put("totalJobs", rows.size()); result.put("overdueCount", rows.stream().filter(r -> r.get("daysRemaining") instanceof Number n && n.longValue() < 0).count()); result.put("byStatus", countBy(rows, "status")); result.put("byPriority", countBy(rows, "priority")); }
            case "supplier-price" -> result.put("totalReplies", rows.size());
        }
        return result;
    }

    private double sum(List<Map<String, Object>> rows, String field) { return rows.stream().mapToDouble(r -> r.get(field) instanceof Number n ? n.doubleValue() : 0).sum(); }
    private long count(List<Map<String, Object>> rows, String field, String expected) { return rows.stream().filter(r -> expected.equalsIgnoreCase(String.valueOf(r.get(field)))).count(); }
    private String value(Map<String, Object> row, String field) { return String.valueOf(row.getOrDefault(field, "")).toLowerCase(Locale.ROOT); }
    private Map<String, Long> countBy(List<Map<String, Object>> rows, String field) { return rows.stream().collect(Collectors.groupingBy(r -> String.valueOf(r.getOrDefault(field, "Unknown")), LinkedHashMap::new, Collectors.counting())); }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getFilterOptions(String reportType, LocalDate start, LocalDate end) {
        Map<String, Object> report = switch (reportType) {
            case "sales" -> getSalesReport(start, end);
            case "inventory" -> getInventoryReport(start, end);
            case "grn" -> getGrnReport(start, end);
            case "purchase-orders" -> getPurchaseOrderReport(start, end);
            case "production" -> getProductionReport(start, end);
            case "supplier-price" -> getSupplierPriceReport(start, end);
            default -> throw new IllegalArgumentException("Unsupported report type");
        };
        List<Map<String, Object>> rows = (List<Map<String, Object>>) report.getOrDefault("rows", List.of());
        Map<String, List<String>> options = new LinkedHashMap<>();
        for (String field : filterFields(reportType)) {
            options.put(field, rows.stream().map(r -> String.valueOf(r.getOrDefault(field, "")))
                    .filter(value -> !value.isBlank() && !"-".equals(value))
                    .distinct().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList()));
        }
        return options;
    }

    private List<String> filterFields(String reportType) {
        return switch (reportType) {
            case "sales" -> List.of("customer", "status");
            case "inventory" -> List.of("variant", "lotType", "status");
            case "grn" -> List.of("supplier", "material", "variant", "batchNo", "receivedBy");
            case "purchase-orders" -> List.of("supplier", "paymentStatus");
            case "production" -> List.of("customer", "status", "priority");
            case "supplier-price" -> List.of("supplier", "material");
            default -> List.of();
        };
    }

    private final QuotationRepo quotationRepo;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final ProductionRepo productionRepo;
    private final PriceRequestReplyRepo priceRequestReplyRepo;
    private final StockLotsRepo stockLotsRepo;
    private final SupplierPaymentRepo supplierPaymentRepo;

    public ReportService(
            QuotationRepo quotationRepo,
            InventoryRepository inventoryRepository,
            PurchaseOrderRepo purchaseOrderRepo,
            ProductionRepo productionRepo,
            PriceRequestReplyRepo priceRequestReplyRepo,
            StockLotsRepo stockLotsRepo,
            SupplierPaymentRepo supplierPaymentRepo) {
        this.quotationRepo = quotationRepo;
        this.inventoryRepository = inventoryRepository;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.productionRepo = productionRepo;
        this.priceRequestReplyRepo = priceRequestReplyRepo;
        this.stockLotsRepo = stockLotsRepo;
        this.supplierPaymentRepo = supplierPaymentRepo;
    }

    // ── 1. Sales / Quotation Summary Report ──────────────
    public Map<String, Object> getSalesReport(LocalDate start, LocalDate end) {
        List<QuotationModel> quotations = quotationRepo.findByQuotationdateBetween(start, end);
        double totalRevenue = quotationRepo.sumRevenueBetween(start, end);
        double totalAdvance = quotations.stream()
                .mapToDouble(QuotationModel::getAdvanceamount).sum();

        Map<String, Long> byStatus = quotations.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getQuotationstatus() != null ? q.getQuotationstatus() : "Unknown",
                        Collectors.counting()));

        long pendingCount = quotations.stream()
                .filter(q -> "Pending".equalsIgnoreCase(q.getQuotationstatus())).count();
        long acceptedCount = quotations.stream()
                .filter(q -> q.getQuotationstatus() != null &&
                        q.getQuotationstatus().toLowerCase().contains("approved")).count();

        List<Map<String, Object>> rows = quotations.stream().map(q -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", "Q-" + q.getId());
            row.put("date", q.getQuotationdate() != null ? q.getQuotationdate().toString() : "-");
            row.put("customer", q.getCustomer() != null ? q.getCustomer().getName() : "-");
            // Build product description/size string
            StringBuilder desc = new StringBuilder();
            if (q.getQuotationdescription() != null && !q.getQuotationdescription().isBlank())
                desc.append(q.getQuotationdescription());
            if (q.getProductsize() != null && !q.getProductsize().isBlank()) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append(q.getProductsize());
            }
            row.put("productSize", desc.length() > 0 ? desc.toString() : "-");
            row.put("quantity", q.getQuantity() != null ? q.getQuantity() : "-");
            row.put("amount", q.getQuotationamount());
            row.put("advanceAmount", q.getAdvanceamount());
            row.put("status", q.getQuotationstatus() != null ? q.getQuotationstatus() : "-");
            row.put("expiryDate", q.getExpiryDate() != null ? q.getExpiryDate().toString() : "-");
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalAdvance", totalAdvance);
        result.put("totalOrders", quotations.size());
        result.put("pendingCount", pendingCount);
        result.put("acceptedCount", acceptedCount);
        result.put("byStatus", byStatus);
        result.put("rows", rows);
        return result;
    }

    // ── 2. Inventory / Stock-Lot Report ──────────────────
    public Map<String, Object> getInventoryReport(LocalDate start, LocalDate end) {
        List<StockLots> lots = stockLotsRepo.findByCreatedAtBetween(start, end);

        int totalQty = lots.stream()
                .mapToInt(l -> l.getQuantity() != null ? l.getQuantity() : 0).sum();
        long belowReorderCount = lots.stream().filter(l -> {
            if (l.getVariant() == null) return false;
            Integer reorder = l.getVariant().getReorderLevel();
            Integer qty = l.getQuantity();
            return reorder != null && qty != null && qty < reorder;
        }).count();

        List<Map<String, Object>> rows = lots.stream().map(l -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("lotId", "LOT-" + l.getId());

            // Build variant description
            String variantStr = "-";
            if (l.getVariant() != null) {
                try {
                    StringBuilder sb = new StringBuilder();
                    if (l.getVariant().getMaterial() != null)
                        sb.append(l.getVariant().getMaterial().getMaterial());
                    if (l.getVariant().getGsm() != null)
                        sb.append(" | ").append(l.getVariant().getGsm()).append(" GSM");
                    Double w = l.getVariant().getWidth() != null ? l.getVariant().getWidth() : l.getWidth();
                    Double h = l.getVariant().getHeight() != null ? l.getVariant().getHeight() : l.getHeight();
                    if (w != null) sb.append(" | ").append(w.intValue()).append("mm");
                    if (h != null) sb.append("×").append(h.intValue()).append("mm");
                    variantStr = sb.length() > 0 ? sb.toString() : "-";
                } catch (Exception ex) {
                    variantStr = "-";
                }
            }
            row.put("variant", variantStr);
            row.put("quantity", l.getQuantity() != null ? l.getQuantity() : "-");
            row.put("unit", l.getUnit() != null ? l.getUnit() : "-");
            row.put("lotType", l.getLotType() != null ? l.getLotType() : "-");
            row.put("status", l.getStatus() != null ? l.getStatus() : "-");

            // Source GRN from linked inventory
            String sourceGrn = "-";
            LocalDate receivedDate = null;
            if (l.getInventory() != null) {
                sourceGrn = l.getInventory().getGrnNumber() != null ? l.getInventory().getGrnNumber() : "-";
                receivedDate = l.getInventory().getReceivedDate();
            }
            row.put("sourceGrn", sourceGrn);
            row.put("receivedDate", receivedDate != null ? receivedDate.toString() : "-");

            // Reorder level comparison
            Integer reorder = l.getVariant() != null ? l.getVariant().getReorderLevel() : null;
            Integer qty = l.getQuantity();
            if (reorder != null && qty != null) {
                row.put("reorderLevel", reorder);
                row.put("belowReorder", qty < reorder);
            } else {
                row.put("reorderLevel", reorder != null ? reorder : "-");
                row.put("belowReorder", false);
            }
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLots", lots.size());
        result.put("totalQty", totalQty);
        result.put("belowReorderCount", belowReorderCount);
        result.put("rows", rows);
        return result;
    }

    // ── 3. GRN Report ────────────────────────────────────
    public Map<String, Object> getGrnReport(LocalDate start, LocalDate end) {
        List<Inventory> grns = inventoryRepository.findByReceivedDateBetween(start, end);

        int totalQtyReceived = grns.stream()
                .mapToInt(i -> i.getReceivedQuantity() != null ? i.getReceivedQuantity() : 0).sum();

        List<Map<String, Object>> rows = grns.stream().map(i -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("grnNumber", i.getGrnNumber() != null ? i.getGrnNumber() : "-");
            row.put("receivedDate", i.getReceivedDate() != null ? i.getReceivedDate().toString() : "-");

            // Supplier name from purchase order
            String supplierName = "-";
            if (i.getPurchaseOrder() != null && i.getPurchaseOrder().getSupplier() != null)
                supplierName = i.getPurchaseOrder().getSupplier().getCompanyname();
            row.put("supplier", supplierName);

            row.put("supplierInvoice", i.getSupplierInvoiceNo() != null ? i.getSupplierInvoiceNo() : "-");
            row.put("batchNo", i.getBatchNo() != null ? i.getBatchNo() : "-");

            // Material/variant string
            String materialStr = "-";
            if (i.getVariant() != null && i.getVariant().getMaterial() != null) {
                try {
                    StringBuilder sb = new StringBuilder(i.getVariant().getMaterial().getMaterial());
                    if (i.getVariant().getGsm() != null) sb.append(" - ").append(i.getVariant().getGsm()).append("gsm");
                    if (i.getVariant().getWidth() != null) sb.append(" x ").append(i.getVariant().getWidth().intValue()).append("mm");
                    if (i.getVariant().getHeight() != null) sb.append("×").append(i.getVariant().getHeight().intValue()).append("mm");
                    materialStr = sb.toString();
                } catch (Exception ex) {
                    materialStr = "-";
                }
            }
            row.put("material", materialStr);
            row.put("variant", materialStr);
            row.put("receivedBy", i.getReceivedByUser() != null ? i.getReceivedByUser().getUsername() : "-");
            row.put("receivedQty", i.getReceivedQuantity() != null ? i.getReceivedQuantity() : "-");
            row.put("units", i.getUnits() != null ? i.getUnits() : "-");
            row.put("notes", i.getNotes() != null ? i.getNotes() : "-");
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalGRNs", grns.size());
        result.put("totalQtyReceived", totalQtyReceived);
        result.put("rows", rows);
        return result;
    }

    // ── 4. Purchase Order Report ──────────────────────────
    public Map<String, Object> getPurchaseOrderReport(LocalDate start, LocalDate end) {
        List<PurchaseOrder> orders = purchaseOrderRepo.findByDateRangeSmart(start, end);
        List<Map<String, Object>> rows = orders.stream().map(o -> {
            SupplierPayment payment = supplierPaymentRepo.findTopByPurchaseOrder_IdOrderByCreatedAtDesc(o.getId()).orElse(null);
            String paymentStatus = payment != null && payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Unpaid";
            String paymentMethod = payment != null ? payment.getPaymentMethod() : null;
            Double paidAmount = payment != null ? payment.getPaidAmount() : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", "PO-" + o.getId());
            row.put("supplier", o.getSupplier() != null ? o.getSupplier().getCompanyname() : "-");
            LocalDate displayDate = o.getOrderDate() != null ? o.getOrderDate() : o.getCreatedDate();
            row.put("orderDate", displayDate != null ? displayDate.toString() : "-");
            row.put("items", o.getItems() != null ? o.getItems() : "-");
            row.put("quantity", o.getQuantity() != null ? o.getQuantity() : "-");
            row.put("paymentStatus", paymentStatus);
            row.put("paymentMethod", paymentMethod != null ? paymentMethod : "-");
            row.put("paidAmount", paidAmount);
            row.put("notes", o.getNotes() != null ? o.getNotes() : "-");
            return row;
        }).collect(Collectors.toList());

        double totalPaid = rows.stream().mapToDouble(r -> {
            Object value = r.get("paidAmount");
            return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
        }).sum();
        long unpaidCount = rows.stream()
                .filter(r -> "Unpaid".equalsIgnoreCase(String.valueOf(r.get("paymentStatus")))).count();

        Map<String, Long> byPaymentStatus = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> String.valueOf(r.get("paymentStatus")),
                        Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", orders.size());
        result.put("totalPaid", totalPaid);
        result.put("unpaidCount", unpaidCount);
        result.put("byPaymentStatus", byPaymentStatus);
        result.put("rows", rows);
        return result;
    }

     //production status report
    public Map<String, Object> getProductionReport(LocalDate start, LocalDate end) {
        // A production report is scoped by when work entered production; deadline is only for urgency.
        List<ProductionModel> jobs = productionRepo.findByDateSentToProductionBetween(start, end);

        LocalDate today = LocalDate.now();
        long overdueCount = jobs.stream()
                .filter(j -> j.getDeadline() != null && j.getDeadline().isBefore(today))
                .count();

        Map<String, Long> byStatus = jobs.stream()
                .collect(Collectors.groupingBy(
                        j -> j.getStatus() != null ? j.getStatus() : "Unknown",
                        Collectors.counting()));

        Map<String, Long> byPriority = jobs.stream()
                .collect(Collectors.groupingBy(
                        j -> j.getPriority() != null ? j.getPriority() : "Unknown",
                        Collectors.counting()));

        List<Map<String, Object>> rows = jobs.stream().map(j -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderId", j.getOrderId() != null ? j.getOrderId() : "-");
            row.put("customer", j.getCustomerName() != null ? j.getCustomerName() : "-");
            row.put("description", j.getDescription() != null ? j.getDescription() : "-");
            row.put("deadline", j.getDeadline() != null ? j.getDeadline().toString() : "-");
            row.put("priority", j.getPriority() != null ? j.getPriority() : "-");
            row.put("status", j.getStatus() != null ? j.getStatus() : "-");
            // Days remaining: negative means overdue
            if (j.getDeadline() != null) {
                long days = ChronoUnit.DAYS.between(today, j.getDeadline());
                row.put("daysRemaining", days);
            } else {
                row.put("daysRemaining", null);
            }
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalJobs", jobs.size());
        result.put("overdueCount", overdueCount);
        result.put("byStatus", byStatus);
        result.put("byPriority", byPriority);
        result.put("rows", rows);
        return result;
    }

    // ── 6. Supplier Price Comparison ──────────────────────
    public Map<String, Object> getSupplierPriceReport(LocalDate start, LocalDate end) {
        List<PriceRequestReply> replies = priceRequestReplyRepo.findAll().stream()
                .filter(r -> r.getReplyDate() != null
                        && !r.getReplyDate().isBefore(start)
                        && !r.getReplyDate().isAfter(end))
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = replies.stream().map(r -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("requestId", r.getPriceRequest() != null ? "PR-" + r.getPriceRequest().getId() : "-");
            row.put("material", r.getPriceRequest() != null ? r.getPriceRequest().getMaterialcategory() : "-");
            row.put("specification", r.getPriceRequest() != null ? r.getPriceRequest().getItemSpecification() : "-");
            row.put("supplier", r.getSupplier() != null ? r.getSupplier().getCompanyname() : "-");
            row.put("unitPrice", r.getUnitPrice());
            row.put("quantity", r.getQuantity());
            row.put("totalAmount", r.getTotalAmount());
            row.put("deliveryCharge", r.getDeliveryCharge());
            row.put("deliveryDate", r.getDeliveryDate());
            row.put("replyDate", r.getReplyDate());
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalReplies", replies.size());
        result.put("rows", rows);
        return result;
    }
}
