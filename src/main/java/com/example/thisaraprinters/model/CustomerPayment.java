package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_payments")
public class CustomerPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "invoice_no", unique = true)
    private String invoiceNo;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_notes")
    private String paymentNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "quotation_id", referencedColumnName = "id")
    private QuotationModel quotation;

    @ManyToOne
    @JoinColumn(name = "production_id", referencedColumnName = "id")
    private ProductionModel production;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerModel customer;
}
