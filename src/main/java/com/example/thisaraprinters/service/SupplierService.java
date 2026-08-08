package com.example.thisaraprinters.service;

import com.example.thisaraprinters.config.EmailService;
import com.example.thisaraprinters.dto.PriceRequestDto;
import com.example.thisaraprinters.dto.PriceRequestReplyDto;
import com.example.thisaraprinters.dto.PurchaseOrderDto;
import com.example.thisaraprinters.dto.SupplierDto;
import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierService {


    private final SupplierRepo supplierRepo;
    private final MaterialRepo materialRepo;
    private final PriceRequestRepo  priceRequestRepo;
    private final PriceRequestReplyRepo priceRequestReplyRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final CategoryRepo categoryRepo;
    private final SupplierPaymentRepo supplierPaymentRepo;
    @Autowired
     private EmailService emailService;
    @Autowired
    private MaterialVariantRepo materialVariantRepo;

    SupplierService(SupplierRepo supplierRepo, MaterialRepo materialRepo, PriceRequestRepo  priceRequestRepo, PriceRequestReplyRepo priceRequestReplyRepo, PurchaseOrderRepo purchaseOrderRepo, CategoryRepo categoryRepo, SupplierPaymentRepo supplierPaymentRepo) {
        this.supplierRepo = supplierRepo;
        this.materialRepo = materialRepo;
        this.priceRequestRepo = priceRequestRepo;
        this.priceRequestReplyRepo = priceRequestReplyRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.categoryRepo = categoryRepo;
        this.supplierPaymentRepo = supplierPaymentRepo;
    }

    public List<Supplier> getAllUsers() {
            return supplierRepo.findAll();
    }

    public List<Supplier> getAllSuppliers() {
        return getAllUsers();
    }
    
    public List<MaterialVariant> getAllMaterials() {
        return materialVariantRepo.findAll();
    }

    public List<Category> getAllCategory(){
        return categoryRepo.findAll();
    }

    public String addSupllier(SupplierDto data) {
        try {
            Supplier supplier = new Supplier();

            //assign supplier's data into new object
            supplier.setCompanyname(data.getCompanyname());
            supplier.setEmail(data.getEmail());
            supplier.setContactperson(data.getContactperson());
            supplier.setContact(data.getContact());
            supplier.setAddress(data.getAddress());
            supplier.setDescription(data.getDescription());
            supplier.setStatus(data.getStatus());
            //find materails id's

            if (data.getCategory() != null) {
                List<Integer> categoryIds = data.getCategory()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());
                List<Category> manageCategories = categoryRepo.findAllById(categoryIds);
                supplier.setCategory(manageCategories);
            }
            //save supplier
            supplierRepo.save(supplier);
            return "supplier added succesfully";
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Supplier getSupplierById(int id) {
        Supplier suplierObject =  supplierRepo.findById(id).get();
        return suplierObject;
    }

    public String updateSupplier(SupplierDto supplierData, int id) {
        try {
            Supplier exsistingSupplier = supplierRepo.findById(id).get();

            exsistingSupplier.setEmail(supplierData.getEmail());
            exsistingSupplier.setCompanyname(supplierData.getCompanyname());
            exsistingSupplier.setContactperson(supplierData.getContactperson());
            exsistingSupplier.setContact(supplierData.getContact());
            exsistingSupplier.setAddress(supplierData.getAddress());
            exsistingSupplier.setDescription(supplierData.getDescription());
            exsistingSupplier.setStatus(supplierData.getStatus());
            exsistingSupplier.setCategory(supplierData.getCategory());
            supplierRepo.save(exsistingSupplier);
            return "update successful";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String deleteSupplier(int id) {
        Supplier supplier = supplierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        if (purchaseOrderRepo.existsBySupplier_Id(id)
                || supplierPaymentRepo.existsBySupplier_Id(id)
                || priceRequestReplyRepo.existsBySupplier_Id(id)
                || priceRequestRepo.existsBySupplierlist_Id(id)) {
            throw new IllegalStateException("This supplier has existing transactions or price requests and cannot be deleted.");
        }

        supplierRepo.delete(supplier);
        return "Supplier deleted successfully";
    }
//create a new price request for price calling
    public String getPricerequest(PriceRequestDto request) {
        try {

            PriceRequest newPriceRequest = new PriceRequest();
            newPriceRequest.setMaterialcategory(request.getMaterialcategory());
            newPriceRequest.setItemSpecification(request.getItemSpecification());
            newPriceRequest.setQuantity(request.getQuantity());
            newPriceRequest.setDeadline(request.getDeadline());
            newPriceRequest.setCreateDate(LocalDate.now());
            newPriceRequest.setMessage(request.getMessage());
            newPriceRequest.setRequeststatus("Pending");
            newPriceRequest.setSupplierlist(request.getSupplierlist());

            PriceRequest savedRequest = priceRequestRepo.save(newPriceRequest);

            //fetch full supplier records from DB (frontend only sends {id}, email would be null otherwise)
            List<Integer> supplierIds = request.getSupplierlist()
                    .stream()
                    .map(Supplier::getId)
                    .collect(Collectors.toList());
            List<Supplier> suppliers = supplierRepo.findAllById(supplierIds);
            String materialCategory = request.getMaterialcategory();
            String itemSpecification = request.getItemSpecification();
            String message = request.getMessage();
            emailService.sendEmailForPriceRequest(suppliers, materialCategory, itemSpecification, message, savedRequest.getId());

            return "Price request send successfully";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Save a supplier's reply to a price request
    public String savePriceRequestReply(PriceRequestReplyDto dto) {
        try {
            PriceRequest priceRequest = priceRequestRepo.findById(dto.getPriceRequestId())
                    .orElseThrow(() -> new RuntimeException("Price request not found"));
            Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));

            PriceRequestReply reply = new PriceRequestReply();
            reply.setUnitPrice(dto.getUnitPrice());
            reply.setDeliveryCharge(dto.getDeliveryCharge());
            reply.setTotalAmount(dto.getTotalAmount());
            reply.setQuantity(dto.getQuantity());
            reply.setDeliveryDate(dto.getDeliveryDate());
            reply.setReplyDate(LocalDate.now());
            reply.setPriceRequest(priceRequest);
            reply.setSupplier(supplier);

            priceRequestReplyRepo.save(reply);

            // Auto-set status to Completed when a supplier replies
            priceRequest.setRequeststatus("Completed");
            priceRequestRepo.save(priceRequest);

            return "Reply submitted successfully";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Get all price requests
    public List<PriceRequest> getAllPriceRequests() {
        return priceRequestRepo.findAll();
    }

    public PriceRequest getPriceRequestById(int id) {
        return priceRequestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Price request not found"));
    }

    public List<Supplier> getSuppliersByPriceRequestCategory(int priceRequestId) {
        PriceRequest priceRequest = priceRequestRepo.findById(priceRequestId)
                .orElseThrow(() -> new RuntimeException("Price request not found"));

        String requestedCategory = priceRequest.getMaterialcategory();
        if (requestedCategory == null || requestedCategory.isBlank()) {
            return List.of();
        }

        return supplierRepo.findAll().stream()
                .filter(supplier -> supplier.getCategory() != null)
                .filter(supplier -> supplier.getCategory().stream()
                        .anyMatch(category -> requestedCategory.equalsIgnoreCase(category.getName())))
                .collect(Collectors.toList());
    }

    // Get all replies for a specific price request
    public List<PriceRequestReply> getRepliesByPriceRequestId(int priceRequestId) {
        return priceRequestReplyRepo.findByPriceRequestId(priceRequestId);
    }
    
    // Get completed price requests
    public List<PriceRequest> getCompletedPriceRequests() {
        return priceRequestRepo.findAll().stream()
                .filter(pr -> "Completed".equalsIgnoreCase(pr.getRequeststatus()))
                .collect(Collectors.toList());
    }

    // Creates a Purchase Order Methods
    public String addPurchaseOrder(PurchaseOrderDto dto) {
        try {
            PurchaseOrder order = new PurchaseOrder();
            order.setSupplier(supplierRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found")));

            if (dto.getPriceRequestId() != null) {
                order.setPriceRequest(priceRequestRepo.findById(dto.getPriceRequestId())
                        .orElseThrow(() -> new RuntimeException("Price Request not found")));
            }

            order.setOrderDate(dto.getOrderDate());
            order.setItems(dto.getItems());
            order.setQuantity(dto.getQuantity());
            order.setNotes(dto.getNotes());
            order.setCreatedDate(LocalDate.now());
            order.setTotalAmount(resolvePurchaseOrderTotalAmount(dto.getPriceRequestId(), dto.getSupplierId()));
            order.setPaidAmount(0.0);

            purchaseOrderRepo.save(order);

//            if (dto.getPaymentStatus() != null && !dto.getPaymentStatus().isBlank()) {
//                SupplierPayment payment = new SupplierPayment();
//                payment.setPurchaseOrder(order);
//                payment.setSupplier(order.getSupplier());
//                payment.setPaymentStatus(dto.getPaymentStatus());
//                payment.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : 0.0);
//                payment.setCreatedAt(LocalDateTime.now());
//                supplierPaymentRepo.save(payment);
//            }
            return "Purchase Order created successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Purchase Order: " + e.getMessage());
        }
    }

    public String updatePurchaseOrder(Integer id, com.example.thisaraprinters.dto.PurchaseOrderDto dto) {
        try {
            com.example.thisaraprinters.model.PurchaseOrder order = purchaseOrderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

            order.setSupplier(supplierRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found")));

            if (dto.getPriceRequestId() != null) {
                order.setPriceRequest(priceRequestRepo.findById(dto.getPriceRequestId())
                        .orElseThrow(() -> new RuntimeException("Price Request not found")));
            } else {
                order.setPriceRequest(null);
            }

            order.setOrderDate(dto.getOrderDate());
            order.setItems(dto.getItems());
            order.setQuantity(dto.getQuantity());
            order.setNotes(dto.getNotes());
            order.setTotalAmount(resolvePurchaseOrderTotalAmount(dto.getPriceRequestId(), dto.getSupplierId()));

            purchaseOrderRepo.save(order);

//            if (dto.getPaymentStatus() != null && !dto.getPaymentStatus().isBlank()) {
//                SupplierPayment payment = supplierPaymentRepo.findTopByPurchaseOrder_IdOrderByCreatedAtDesc(id)
//                        .orElseGet(() -> {
//                            SupplierPayment p = new SupplierPayment();
//                            p.setPurchaseOrder(order);
//                            p.setSupplier(order.getSupplier());
//                            return p;
//                        });
//                payment.setPaymentStatus(dto.getPaymentStatus());
//                payment.setSupplier(order.getSupplier());
//                payment.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : 0.0);
//                payment.setCreatedAt(LocalDateTime.now());
//                supplierPaymentRepo.save(payment);
//            }
            return "Purchase Order updated successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to update Purchase Order: " + e.getMessage());
        }
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepo.findAll().stream()
                .peek(this::attachLatestPaymentInfo)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPurchaseOrdersForGrn() {
        return getAllPurchaseOrders().stream()
                .map(order -> {
                    Map<String, Object> purchaseOrder = new LinkedHashMap<>();
                    purchaseOrder.put("id", order.getId());
                    purchaseOrder.put("poNumber", order.getId() != null ? "PO-" + order.getId() : "");
                    purchaseOrder.put("items", order.getItems());
                    purchaseOrder.put("supplierId", order.getSupplier() != null ? order.getSupplier().getId() : null);
                    purchaseOrder.put("supplierName", order.getSupplier() != null ? order.getSupplier().getCompanyname() : "");
                    return purchaseOrder;
                })
                .collect(Collectors.toList());
    }

    public String deletePurchaseOrder(Integer id) {
        try {
            supplierPaymentRepo.findByPurchaseOrder_Id(id)
                    .forEach(supplierPaymentRepo::delete);
            purchaseOrderRepo.deleteById(id);
            return "Purchase Order deleted successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete Purchase Order: " + e.getMessage());
        }
    }

    public String updatePaymentStatus(Integer orderId, String paymentStatus, String paymentMethod, Double paidAmount, String paymentProofFileName, String paymentNotes) {
        try {
            com.example.thisaraprinters.model.PurchaseOrder order = purchaseOrderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

            double orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            double currentPaid = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
            double newPaymentAmount = paidAmount != null ? paidAmount : 0.0;
            double updatedPaidAmount = currentPaid + newPaymentAmount;

            String resolvedStatus;
            if (orderTotal > 0 && updatedPaidAmount < orderTotal) {
                resolvedStatus = "Partial";
            } else if (orderTotal > 0 && Double.compare(updatedPaidAmount, orderTotal) == 0) {
                resolvedStatus = "Paid";
            } else {
                resolvedStatus = (paymentStatus != null && !paymentStatus.isBlank()) ? paymentStatus : "Unpaid";
            }

            SupplierPayment payment = new SupplierPayment();
            payment.setPurchaseOrder(order);
            payment.setSupplier(order.getSupplier());
            payment.setPaymentStatus(resolvedStatus);
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                payment.setPaymentMethod(paymentMethod);
            }
            payment.setPaidAmount(newPaymentAmount);
            if (paymentProofFileName != null && !paymentProofFileName.isEmpty()) {
                payment.setPaymentProof(paymentProofFileName);
            }
            if (paymentNotes != null && !paymentNotes.isEmpty()) {
                payment.setPaymentNotes(paymentNotes);
            }
            payment.setCreatedAt(LocalDateTime.now());
            supplierPaymentRepo.save(payment);

            order.setPaidAmount(updatedPaidAmount);
            order.setPaymentStatus(resolvedStatus);
            purchaseOrderRepo.save(order);

            attachLatestPaymentInfo(order);
            return "Payment status updated successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to update payment status: " + e.getMessage());
        }
    }

    private void attachLatestPaymentInfo(com.example.thisaraprinters.model.PurchaseOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }

        List<SupplierPayment> payments = supplierPaymentRepo.findByPurchaseOrder_Id(order.getId());
        if (payments == null || payments.isEmpty()) {
            order.setPaymentStatus("Unpaid");
            order.setPaymentMethod(null);
            order.setPaidAmount(0.0);
            order.setPaymentProof(null);
            order.setPaymentNotes(null);
            return;
        }

        SupplierPayment latest = payments.stream()
                .max((a, b) -> a.getCreatedAt() == null || b.getCreatedAt() == null
                        ? 0
                        : a.getCreatedAt().compareTo(b.getCreatedAt()))
                .orElse(null);

        double paidSum = payments.stream()
                .mapToDouble(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : 0.0)
                .sum();

        order.setPaidAmount(paidSum);
        order.setPaymentStatus(latest != null && latest.getPaymentStatus() != null ? latest.getPaymentStatus() : "Unpaid");
        order.setPaymentMethod(latest != null ? latest.getPaymentMethod() : null);
        order.setPaymentProof(latest != null ? latest.getPaymentProof() : null);
        order.setPaymentNotes(latest != null ? latest.getPaymentNotes() : null);
    }

    private Double resolvePurchaseOrderTotalAmount(Integer priceRequestId, Integer supplierId) {
        if (priceRequestId == null || supplierId == null) {
            return 0.0;
        }

        return priceRequestReplyRepo.findByPriceRequestId(priceRequestId).stream()
                .filter(reply -> reply.getSupplier() != null && reply.getSupplier().getId() != null)
                .filter(reply -> reply.getSupplier().getId().equals(supplierId))
                .map(PriceRequestReply::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .max(Double::compareTo)
                .orElse(0.0);
    }
}
