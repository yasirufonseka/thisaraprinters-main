package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.CustomerPaymentDto;
import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.jspecify.annotations.Nullable;
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

    // Supplier Payment
    public List<SupplierPayment> getAllSupplierPayments() {
        return supplierPaymentRepo.findAll();
    }

    // Returns all purchase orders with their latest payment info attached.

    public List<PurchaseOrder> getAllSupplierOrders() {
        return purchaseOrderRepo.findAll().stream()
                .peek(this::attachLatestPaymentInfo)
                .collect(Collectors.toList());
    }

    /**
     * Delegates supplier payment update to the same logic already used in
     * SupplierService,
     * so the payment module stays in sync with the supplier module data.
     */
    public String updateSupplierPayment(
            Integer orderId, String paymentStatus,
            String paymentMethod, Double paidAmount,
            String paymentProofFileName, String paymentNotes) {

        //find the order 
        PurchaseOrder order = purchaseOrderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        //check whether the order is already paid
        if(order.getPaidAmount() >= order.getTotalAmount() ){
            throw new RuntimeException("Order is already fully paid");
        }

        double orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        System.out.println(orderTotal);
        System.out.println(order.getTotalAmount());

        // if (orderTotal <= 0.0 && order.getTotalAmount() != null && order.getTotalAmount() != null) {
        //     orderTotal = order.getTotalAmount();
        // }

        double currentPaid = supplierPaymentRepo.findByPurchaseOrder_Id(orderId).stream()
                .mapToDouble(payment -> payment.getPaidAmount() != null ? payment.getPaidAmount() : 0.0)
                .sum();
        double newPaymentAmount = paidAmount != null ? paidAmount : 0.0;
        double updatedPaidAmount = currentPaid + newPaymentAmount;
        System.out.println(updatedPaidAmount);
        System.out.println(currentPaid);
        System.out.println(newPaymentAmount);

        if(updatedPaidAmount > orderTotal) {
            throw new RuntimeException("Paid amount is greater than the order total");
        }
        
        String resolvedStatus = (paymentStatus != null && !paymentStatus.isBlank()) ? paymentStatus : "Unpaid";
        if (orderTotal > 0.0) {
            if (updatedPaidAmount < orderTotal) {
                resolvedStatus = "Partial";
            } else if (Double.compare(updatedPaidAmount, orderTotal) == 0) {
                resolvedStatus = "Paid";
            }
        }

        SupplierPayment payment = new SupplierPayment();
        payment.setPurchaseOrder(order);
        payment.setSupplier(order.getSupplier());
        payment.setPaymentStatus(resolvedStatus);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaidAmount(newPaymentAmount);
        payment.setPaymentProof(paymentProofFileName);
        payment.setPaymentNotes(paymentNotes);
        payment.setCreatedAt(LocalDateTime.now());
        supplierPaymentRepo.save(payment);

        order.setPaidAmount(updatedPaidAmount);
        order.setPaymentStatus(resolvedStatus);
        purchaseOrderRepo.save(order);

        return "Supplier payment recorded successfully (Status: " + resolvedStatus + ")";
    }

    private void attachLatestPaymentInfo(PurchaseOrder order) {
        if (order == null || order.getId() == null)
            return;

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

        order.setPaymentStatus(
                latest != null && latest.getPaymentStatus() != null ? latest.getPaymentStatus() : "Unpaid");
        order.setPaymentMethod(latest != null ? latest.getPaymentMethod() : null);
        order.setPaidAmount(paidSum);
        order.setPaymentProof(latest != null ? latest.getPaymentProof() : null);
        order.setPaymentNotes(latest != null ? latest.getPaymentNotes() : null);
    }

    // Customer Payment Methods

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

        // Auto-resolve payment status: Partial if paidAmount < balance due, Paid if >=
        // balance
        String resolvedStatus = customerPayments.getPaymentStatus();
        double quotationTotal = quotation.getQuotationamount();
        double advancePaid = quotation.getAdvanceamount();
        double balanceDue = quotationTotal - advancePaid;
        double paid = customerPayments.getPaidAmount() != null ? customerPayments.getPaidAmount() : 0.0;

        if ("Paid".equalsIgnoreCase(resolvedStatus) || "Partial".equalsIgnoreCase(resolvedStatus)) {
            if (balanceDue > 0) {
                resolvedStatus = paid < balanceDue ? "Partial" : "Paid";
            }
        }

        payment.setPaymentStatus(resolvedStatus);
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

        // Auto-resolve payment status based on paid amount vs balance due
        String resolvedStatus = dto.getPaymentStatus();
        if (payment.getQuotation() != null
                && ("Paid".equalsIgnoreCase(resolvedStatus) || "Partial".equalsIgnoreCase(resolvedStatus))) {
            double quotationTotal = payment.getQuotation().getQuotationamount();
            double advancePaid = payment.getQuotation().getAdvanceamount();
            double balanceDue = quotationTotal - advancePaid;
            double paid = dto.getPaidAmount() != null ? dto.getPaidAmount() : 0.0;
            if (balanceDue > 0) {
                resolvedStatus = paid < balanceDue ? "Partial" : "Paid";
            }
        }

        payment.setPaymentStatus(resolvedStatus);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaidAmount(dto.getPaidAmount());
        payment.setReferenceNo(dto.getReferenceNo());
        payment.setPaymentNotes(dto.getPaymentNotes());
        payment.setPaymentDate(LocalDate.now());

        customerPaymentRepo.save(payment);
        return "Payment updated successfully (Status: " + resolvedStatus + ")";
    }

    /**
     * Fetches all data needed to render the invoice for a given customer payment
     * id.
     */
    public CustomerPayment getInvoiceData(Integer paymentId) {
        return customerPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public List<ProductionModel> getAllProductions() {

        return productionRepo.findAll();
    }
}
