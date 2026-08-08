package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.QuotationDto;
import com.example.thisaraprinters.model.CustomerModel;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.model.Materials;
import com.example.thisaraprinters.model.QuotationModel;
import com.example.thisaraprinters.repository.CustomerRepo;
import com.example.thisaraprinters.repository.MaterialRepo;
import com.example.thisaraprinters.repository.MaterialVariantRepo;
import com.example.thisaraprinters.repository.QuotationRepo;
import com.example.thisaraprinters.repository.PriceRequestReplyRepo;
import com.example.thisaraprinters.repository.PurchaseOrderRepo;
import com.example.thisaraprinters.model.PriceRequest;
import com.example.thisaraprinters.model.PriceRequestReply;
import com.example.thisaraprinters.model.PurchaseOrder;
import com.example.thisaraprinters.model.ProductionModel;
import com.example.thisaraprinters.repository.ProductionRepo;
import com.example.thisaraprinters.repository.SupplierPaymentRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final MaterialVariantRepo materialVariantRepo;
    private CustomerRepo customerRepo;
    private MaterialRepo materialsRepo;
    private QuotationRepo quotationRepo;
    private final PriceRequestReplyRepo priceRequestReplyRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final ProductionRepo productionRepo;
    private final SupplierPaymentRepo supplierPaymentRepo;
    private final ProductionService productionService;

    public OrderService(MaterialRepo materialsRepo, CustomerRepo customerRepo, QuotationRepo quotationRepo, 
                        MaterialVariantRepo materialVariantRepo, PriceRequestReplyRepo priceRequestReplyRepo, 
                        PurchaseOrderRepo purchaseOrderRepo, ProductionRepo productionRepo, SupplierPaymentRepo supplierPaymentRepo,
                        ProductionService productionService) {
        this.customerRepo = customerRepo;
        this.materialsRepo = materialsRepo;
        this.quotationRepo = quotationRepo;
        this.materialVariantRepo = materialVariantRepo;
        this.priceRequestReplyRepo = priceRequestReplyRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.productionRepo = productionRepo;
        this.supplierPaymentRepo = supplierPaymentRepo;
        this.productionService = productionService;
    }

    @Transactional
    public String saveQuotation(QuotationDto quotation) {
        // Input Validation
        if (quotation.getQuantity() == null || quotation.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quotation.getMaterialsList() == null || quotation.getMaterialsList().isEmpty()) {
            throw new IllegalArgumentException("Materials should not be empty");
        }

        // Fetch Customer Model if present
        CustomerModel resolvedCustomer = null;
        if (quotation.getCustomer() != null) {
            resolvedCustomer = customerRepo.findById(quotation.getCustomer().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + quotation.getCustomer().getId()));
        }

        // Fetch Material Variants from repository
        List<MaterialVariant> resolvedMaterials = new ArrayList<>();
        for (MaterialVariant mv : quotation.getMaterialsList()) {
            if (mv.getId() != null) {
                MaterialVariant variant = materialVariantRepo.findById(mv.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Material variant not found with ID: " + mv.getId()));
                resolvedMaterials.add(variant);
            }
        }

        // Authoritative Server-Side Cost Verification
        int quantity = quotation.getQuantity();
        int wastageSheets = quotation.getWastageSheets();
        Double edgeMarginMm = quotation.getEdgeMarginMm() != null ? quotation.getEdgeMarginMm() : 10.0;
        double paperRatePerSheet = quotation.getPaperRatePerSheet();
        double finishingCost = quotation.getFinishingCost();
        // impressionCost field holds the raw rate-per-impression entered by the user.
        // Mirror frontend logic: total = rate * max(quantity, 1000)
        double impressionCostRate = quotation.getImpressionCost();
        double totalImpressionCost = impressionCostRate * (quantity < 1000 ? 1000 : quantity);
        double serviceChargePercentage = quotation.getServiceChargePercentage();

        // Calculate optimal products per sheet (imposition)
        // Mirrors frontend: floor(sheetWidth / (prodWidth + margin)) * floor(sheetHeight / (prodHeight + margin))
        // No usable-area subtraction, no gutter — consistent with frontend calculateProductsPerSheet()
        double stockWidth = 0.0;
        double stockHeight = 0.0;
        if (!resolvedMaterials.isEmpty()) {
            MaterialVariant primaryVariant = resolvedMaterials.get(0);
            stockWidth = primaryVariant.getWidth() != null ? primaryVariant.getWidth() : 0.0;
            stockHeight = primaryVariant.getHeight() != null ? primaryVariant.getHeight() : 0.0;
        }
        // product dimensions
        double[] prodDims = getProductDimensionsMm(quotation.getProductsize());
        double prodWidth = prodDims[0];
        double prodHeight = prodDims[1];

        int productsPerSheet = 1; // default to 1 to avoid division by zero
        if (prodWidth > 0 && prodHeight > 0 && stockWidth > 0 && stockHeight > 0) {
            int cuts1 = (int) Math.floor(stockWidth / (prodWidth + edgeMarginMm))
                    * (int) Math.floor(stockHeight / (prodHeight + edgeMarginMm));
            int cuts2 = (int) Math.floor(stockWidth / (prodHeight + edgeMarginMm))
                    * (int) Math.floor(stockHeight / (prodWidth + edgeMarginMm));
            productsPerSheet = Math.max(cuts1, cuts2);
        }

        if (productsPerSheet <= 0) {
            throw new IllegalArgumentException("Imposition error: finished product does not fit on the sheet with the configured edge margin.");
        }

        // Mirror frontend: printedSheets = ceil((quantity + wastageSheets) / productsPerSheet)
        // Wastage is baked into the numerator — totalSheets == printedSheets
        int serverTotalSheets = (int) Math.ceil((double)(quantity + wastageSheets) / productsPerSheet);
        double serverPaperCost = serverTotalSheets * paperRatePerSheet;
        double serverBaseCost = serverPaperCost + finishingCost + totalImpressionCost;
        double serverServiceCharge = serverBaseCost * (serviceChargePercentage / 100.0);
        double serverTotalAmount = serverBaseCost + serverServiceCharge;
        double serverUnitPrice = quantity > 0 ? (serverTotalAmount / quantity) : 0.0;

        // Create a new quotation, or update the requested existing quotation.
        QuotationModel newQuotation = quotation.getId() > 0
                ? quotationRepo.findById(quotation.getId())
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with ID: " + quotation.getId()))
                : new QuotationModel();

        newQuotation.setProductsize(quotation.getProductsize() != null ? quotation.getProductsize().toUpperCase() : null);
        newQuotation.setQuantity(quantity);
        newQuotation.setColor(quotation.getColor());
        newQuotation.setCustomer(resolvedCustomer);
        newQuotation.setQuotationdescription(quotation.getQuotationdescription() != null ? quotation.getQuotationdescription().toUpperCase() : null);
        newQuotation.setCuttingtype(quotation.getCuttingtype() != null ? quotation.getCuttingtype().toUpperCase() : null);
        newQuotation.setFoiling(quotation.getFoiling());
        newQuotation.setLamination(quotation.getLamination());
        newQuotation.setBindingtype(quotation.getBindingtype() != null ? quotation.getBindingtype().toUpperCase() : null);
        // Store the paper type from the server-resolved material variant rather
        // than a client supplied value. This keeps the quotation record aligned
        // with the material actually used for pricing and imposition.
        String paperType = "N/A";
        if (!resolvedMaterials.isEmpty()
                && resolvedMaterials.get(0).getMaterial() != null
                && resolvedMaterials.get(0).getMaterial().getMaterial() != null
                && !resolvedMaterials.get(0).getMaterial().getMaterial().isBlank()) {
            paperType = resolvedMaterials.get(0).getMaterial().getMaterial().trim();
        }
        newQuotation.setPapertype(paperType);
        newQuotation.setAdvanceamount(quotation.getAdvanceamount());
        newQuotation.setQuotationstatus(quotation.getQuotationstatus() != null ? quotation.getQuotationstatus().toUpperCase() : "PENDING");
        if (quotation.getId() == 0) {
            newQuotation.setQuotationdate(LocalDate.now());
            newQuotation.setExpiryDate(calculateExpiryDate());
        }
        newQuotation.setMaterialsList(resolvedMaterials);

        // Set cost breakdown details
        newQuotation.setWastageSheets(wastageSheets != 0 ? wastageSheets : 0);
        newQuotation.setEdgeMarginMm(edgeMarginMm);
        newQuotation.setPaperRatePerSheet(paperRatePerSheet);
        newQuotation.setPaperCost(serverPaperCost);
        newQuotation.setFinishingCost(finishingCost);
        // Store the computed total impression cost (not the raw per-unit rate)
        newQuotation.setImpressionCost(totalImpressionCost);
        newQuotation.setServiceChargePercentage(serviceChargePercentage);
        newQuotation.setServiceChargeAmount(serverServiceCharge);
        newQuotation.setQuotationamount(serverTotalAmount);
        newQuotation.setUnitPrice(serverUnitPrice);

        try {
            quotationRepo.save(newQuotation);
            return "Quotation saved successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to save quotation: " + e.getMessage(), e);
        }
    }

    @Transactional
    private double[] getProductDimensionsMm(String productSize) {
        if (productSize == null) {
            return new double[]{0.0, 0.0};
        }
        String size = productSize.toUpperCase().trim();
        if (size.equals("A3")) {
            return new double[]{297.0, 420.0};
        } else if (size.equals("A4")) {
            return new double[]{210.0, 297.0};
        } else if (size.equals("A5")) {
            return new double[]{148.0, 210.0};
        } else if (size.equals("LETTER")) {
            return new double[]{215.9, 279.4};
        } else if (size.equals("LEGAL")) {
            return new double[]{215.9, 355.6};
        } else if (size.contains("CUSTOM")) {
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[xX]\\s*(\\d+(?:\\.\\d+)?)");
                java.util.regex.Matcher m = p.matcher(size);
                if (m.find()) {
                    double heightInches = Double.parseDouble(m.group(1));
                    double widthInches = Double.parseDouble(m.group(2));
                    return new double[]{widthInches * 25.4, heightInches * 25.4};
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return new double[]{0.0, 0.0};
    }

    private LocalDate calculateExpiryDate(){
        LocalDate createdDate = LocalDate.now();
        LocalDate expiryDate = createdDate.plusDays(7);
        return expiryDate;
    }

    public List<CustomerModel> getAllCustomers() {
        return customerRepo.findAll();
    }

    public List<MaterialVariant> getAllMaterialVariant() {
        List<MaterialVariant> variants = materialVariantRepo.findAll();
        for (MaterialVariant variant : variants) {
            variant.setSheetRate(getLatestSheetRate(variant.getId()));
        }
        return variants;
    }

    public MaterialVariant getMaterialVariantById(Integer id) {
        return materialVariantRepo.findById(id).orElse(null);
    }

    public Double getLatestSheetRate(Integer variantId) {
        MaterialVariant variant = materialVariantRepo.findById(variantId).orElse(null);
        if (variant == null) {
            return 0.0;
        }

        //  checking PriceRequestReply
        try {
            List<PriceRequestReply> replies = priceRequestReplyRepo.findAll();
            for (PriceRequestReply reply : replies) {
                PriceRequest pr = reply.getPriceRequest();
                if (pr != null && pr.getItemSpecification() != null) {
                    String spec = pr.getItemSpecification().toLowerCase();
                    String matName = variant.getMaterial() != null ? variant.getMaterial().getMaterial().toLowerCase() : "";
                    if (!matName.isEmpty() && spec.contains(matName) && spec.contains(variant.getGsm().toString())) {
                        Double price = reply.getUnitPrice();
                        if (price != null && price > 0) {
                            if (spec.contains("ream") || (variant.getSheetsPerReam() != null && variant.getSheetsPerReam() > 0 && price > 100)) {
                                int sheets = (variant.getSheetsPerReam() != null && variant.getSheetsPerReam() > 0) ? variant.getSheetsPerReam() : 500;
                                return price / sheets;
                            }
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }

        //  Try checking PurchaseOrder
        try {
            List<PurchaseOrder> orders = purchaseOrderRepo.findAll();
            for (PurchaseOrder order : orders) {
                String items = order.getItems();
                if (items != null) {
                    String itemsLower = items.toLowerCase();
                    String matName = variant.getMaterial() != null ? variant.getMaterial().getMaterial().toLowerCase() : "";
                    if (!matName.isEmpty() && itemsLower.contains(matName) && itemsLower.contains(variant.getGsm().toString())) {
                        Double paidAmount = supplierPaymentRepo.findTopByPurchaseOrder_IdOrderByCreatedAtDesc(order.getId())
                        .map(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : 0.0)
                        .orElse(0.0);
                if (paidAmount > 0) {
                            try {
                                double qty = Double.parseDouble(order.getQuantity());
                                if (qty > 0) {
                                    double unitCost = paidAmount / qty;
                                    if (itemsLower.contains("ream") || (variant.getSheetsPerReam() != null && variant.getSheetsPerReam() > 0 && unitCost > 100)) {
                                        int sheets = (variant.getSheetsPerReam() != null && variant.getSheetsPerReam() > 0) ? variant.getSheetsPerReam() : 500;
                                        return unitCost / sheets;
                                    }
                                    return unitCost;
                                }
                            } catch (NumberFormatException nfe) {
                                // ignore
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }

        // Fallback: Dynamic formula based on GSM and dimensions
        double gsm = variant.getGsm() != null ? variant.getGsm() : 80.0;
        double width = variant.getWidth() != null ? variant.getWidth() : 297.0;
        double height = variant.getHeight() != null ? variant.getHeight() : 420.0;
        double weightInKg = (width * height * gsm) / 1000000000.0;
        double ratePerKg = 750.0;
        double calculatedRate = weightInKg * ratePerKg;

        if (calculatedRate <= 0) {
            calculatedRate = 5.0;
        }

        return Math.round(calculatedRate * 100.0) / 100.0;
    }

    public List<QuotationModel> getAllQuotations() {
        return quotationRepo.findAll();
    }

    @Transactional
    public void deleteQuotation(int id) {
        if (!quotationRepo.existsById(id)) {
            throw new IllegalArgumentException("Quotation not found with ID: " + id);
        }
        quotationRepo.deleteById(id);
    }

    @Transactional
    public String sendToProduction(int quotationId, String priority, LocalDate deadline, double advanceAmount) {
        QuotationModel quotation = quotationRepo.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with ID: " + quotationId));

        String orderId = "ORD-" + quotationId;
        if (productionRepo.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Quotation is already in production (Order ID: " + orderId + ")");
        }

        ProductionModel job = new ProductionModel();
        job.setOrderId(orderId);
        job.setCustomerName(quotation.getCustomer() != null ? quotation.getCustomer().getName() : "Walk-in Customer");
        job.setQuotationid(quotation);
        
        StringBuilder descBuilder = new StringBuilder();
        if (quotation.getQuotationdescription() != null && !quotation.getQuotationdescription().isEmpty()) {
            descBuilder.append(quotation.getQuotationdescription()).append(" ");
        }
        descBuilder.append("(Size: ").append(quotation.getProductsize())
                   .append(", Qty: ").append(quotation.getQuantity())
                   .append(", Color: ").append(quotation.getColor())
                   .append(")");
        job.setDescription(descBuilder.toString());
        job.setDeadline(deadline != null ? deadline : (quotation.getExpiryDate() != null ? quotation.getExpiryDate() : LocalDate.now().plusDays(7)));
        job.setPriority(priority != null ? priority : "Normal");
        job.setStatus("New Orders");

        // Derive total sheets from the quotation's saved cost breakdown:
        // paperCost = totalSheets * paperRatePerSheet  →  totalSheets = paperCost / paperRatePerSheet
        int totalSheetsNeeded = 0;
        if (quotation.getPaperRatePerSheet() > 0) {
            totalSheetsNeeded = (int) Math.round(quotation.getPaperCost() / quotation.getPaperRatePerSheet());
        }
        job.setTotalSheetsNeeded(totalSheetsNeeded);

        productionService.createJob(job);

        // Save advance amount paid at time of production approval
        quotation.setAdvanceamount(advanceAmount);
        quotation.setQuotationstatus("APPROVED");
        quotationRepo.save(quotation);

        return "Sent to Production successfully";
    }
}
