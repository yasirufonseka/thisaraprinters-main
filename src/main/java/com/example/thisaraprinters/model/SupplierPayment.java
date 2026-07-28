package com.example.thisaraprinters.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "supplier_payment")
public class SupplierPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_proof")
    private String paymentProof;

    @Column(name = "payment_notes")
    private String paymentNotes;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "purchase_orders_id", referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "suppliers_id", referencedColumnName = "id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "users_id", referencedColumnName = "id")
    private UserModel user;
}
