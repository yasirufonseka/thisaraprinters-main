package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "purchase_orders")
@Data
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "price_request_id", referencedColumnName = "id")
    private PriceRequest priceRequest;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "items")
    private String items;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "status")
    private String status;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Transient
    private String paymentStatus;

    @Transient
    private String paymentMethod;

    @Transient
    private String paymentProof;

    @Transient
    private String paymentNotes;
}
