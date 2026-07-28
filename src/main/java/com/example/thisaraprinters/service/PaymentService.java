package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.CustomerPaymentDto;
import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PurchaseOrderRepo purchaseOrderRepo;
    private final SupplierPaymentRepo supplierPaymentRepo;
    private final CustomerPaymentRepo customerPaymentRepo;
    private final QuotationRepo quotationRepo;
    private final CustomerRepo customerRepo;
    private final ProductionRepo productionRepo;

    public PaymentService(PurchaseOrderRepo purchaseOrderRepo,
                          SupplierPaymentRepo supplierPaymentRepo,
                          CustomerPaymentRepo customerPaymentRepo,
                          QuotationRepo quotationRepo,
                          CustomerRepo customerRepo,
                          ProductionRepo productionRepo) {
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.supplierPaymentRepo = supplierPaymentRepo;
        this.customerPaymentRepo = customerPaymentRepo;
        this.quotationRepo = quotationRepo;
        this.customerRepo = customerRepo;
        this.productionRepo = productionRepo;
    }

    //  Supplier Payment

    
     // Returns all purchase orders with their latest payment info attached.
     
    public List<PurchaseOrder> getAllSupplierOrders() {
        return purchaseOrderRepo.findAll().stream()
                .peek(this::attachLatestPaymentInfo)
                .collect(Collectors.toList());
    }

    /**
     * Delegates supplier payment update to the same logic already used in SupplierService,
     * so the payment module stays in sync with the supplier module data.
     */
    public String updateSupplierPayment(Integer orderId, String paymentStatus,
                                        String paymentMethod, Double paidAmount,
                                        String paymentProofFileName, String paymentNotes) {
        PurchaseOrder order = purchaseOrderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        String resolvedStatus = (paymentStatus != null && !paymentStatus.isBlank())
                ? paymentStatus : "Unpaid";

        SupplierPayment payment = supplierPaymentRepo
                .findTopByPurchaseOrder_IdOrderByCreatedAtDesc(orderId)
                .orElseGet(SupplierPayment::new);

        payment.setPurchaseOrder(order);
        payment.setSupplier(order.getSupplier());
        payment.setPaymentStatus(resolvedStatus);
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            payment.setPaymentMethod(paymentMethod);
        }
        if (paidAmount != null) {
            payment.setPaidAmount(paidAmount);
        }
        if (paymentProofFileName != null && !paymentProofFileName.isEmpty()) {
            payment.setPaymentProof(paymentProofFileName);
        }
        if (paymentNotes != null && !paymentNotes.isEmpty()) {
            payment.setPaymentNotes(paymentNotes);
        }
        payment.setCreatedAt(LocalDateTime.now());
        supplierPaymentRepo.save(payment);
        return "Supplier payment updated successfully";
    }

    private void attachLatestPaymentInfo(PurchaseOrder order) {
        if (order == null || order.getId() == null) return;
        SupplierPayment payment = supplierPaymentRepo
                .findTopByPurchaseOrder_IdOrderByCreatedAtDesc(order.getId())
                .orElse(null);
        if (payment == null) {
            order.setPaymentStatus("Unpaid");
            order.setPaymentMethod(null);
            order.setPaidAmount(0.0);
            order.setPaymentProof(null);
            order.setPaymentNotes(null);
        } else {
            order.setPaymentStatus(payment.getPaymentStatus());
            order.setPaymentMethod(payment.getPaymentMethod());
            order.setPaidAmount(payment.getPaidAmount());
            order.setPaymentProof(payment.getPaymentProof());
            order.setPaymentNotes(payment.getPaymentNotes());
        }
    }

    // ─── Customer Payment Methods ─────────────────────────────────────────────

    /**
     * Returns all customer payments with their linked quotation and customer info.
     */
    public List<CustomerPayment> getAllCustomerPayments() {
        return customerPaymentRepo.findAll();
    }

    /**
     * Returns all quotations — used to populate the customer payments tab
     * so staff can record payments against any quotation.
     */
    public List<QuotationModel> getAllQuotations() {
        return quotationRepo.findAll();
    }

    /**
     * Records a new payment for a customer quotation.
     * Generates an invoice number in the format INV-YYYYMMDD-{id}.
     */
    public String addCustomerPayment(CustomerPaymentDto customerPayments) {
        QuotationModel quotation = null;
        if (customerPayments.getQuotationId() != null) {
            quotation = quotationRepo.findById(customerPayments.getQuotationId()).orElse(null);
        }

        ProductionModel production = null;
        if (customerPayments.getProductionId() != null) {
            production = productionRepo.findById(customerPayments.getProductionId()).orElse(null);
        }

        if (quotation == null && production != null && production.getQuotationid() != null) {
            quotation = production.getQuotationid();
        }

        if (quotation == null) {
            throw new RuntimeException("Quotation not found");
        }

        CustomerModel customer = null;
        if (customerPayments.getCustomerId() != null) {
            customer = customerRepo.findById(customerPayments.getCustomerId()).orElse(null);
        }
        if (customer == null && quotation.getCustomer() != null) {
            customer = quotation.getCustomer();
        }
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        CustomerPayment payment = null;
        if (customerPaymentRepo.existsByQuotation_Id(quotation.getId())) {
            payment = customerPaymentRepo.findByQuotation_Id(quotation.getId()).orElse(null);
        } else if (production != null && customerPaymentRepo.existsByProduction_Id(production.getId())) {
            payment = customerPaymentRepo.findByProduction_Id(production.getId()).orElse(null);
        }

        if (payment == null) {
            payment = new CustomerPayment();
            payment.setCreatedAt(LocalDateTime.now());
        }

        payment.setQuotation(quotation);
        payment.setProduction(production);
        payment.setCustomer(customer);
        payment.setPaymentStatus(customerPayments.getPaymentStatus());
        payment.setPaymentMethod(customerPayments.getPaymentMethod());
        payment.setPaidAmount(customerPayments.getPaidAmount());
        payment.setReferenceNo(customerPayments.getReferenceNo());
        payment.setPaymentNotes(customerPayments.getPaymentNotes());
        payment.setPaymentDate(LocalDate.now());

        CustomerPayment saved = customerPaymentRepo.save(payment);

        // Generate invoice number after save (needs the DB-assigned id)
        if (saved.getInvoiceNo() == null || saved.getInvoiceNo().isBlank()) {
            String invoiceNo = "INV-" + LocalDate.now().toString().replace("-", "") + "-" + saved.getId();
            saved.setInvoiceNo(invoiceNo);
            customerPaymentRepo.save(saved);
        }

        return "Customer payment recorded successfully. Invoice: " + saved.getInvoiceNo();
    }

    /**
     * Updates an existing customer payment record.
     */
    public String updateCustomerPayment(Integer id, CustomerPaymentDto dto) {
        CustomerPayment payment = customerPaymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaidAmount(dto.getPaidAmount());
        payment.setReferenceNo(dto.getReferenceNo());
        payment.setPaymentNotes(dto.getPaymentNotes());
        payment.setPaymentDate(LocalDate.now());

        customerPaymentRepo.save(payment);
        return "Payment updated successfully";
    }

    /**
     * Fetches all data needed to render the invoice for a given customer payment id.
     */
    public CustomerPayment getInvoiceData(Integer paymentId) {
        return customerPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }
}
