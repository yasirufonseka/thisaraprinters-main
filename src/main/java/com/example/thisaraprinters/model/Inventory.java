package com.example.thisaraprinters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name="inventory")
public class Inventory {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "grnnumber")
    private String grnNumber;

    @Column(name = "supplierinvoiceno")
    private String supplierInvoiceNo;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "receivedquantity")
    private Integer receivedQuantity;

    @Column(name = "units")
    private String units;

    @Column(name = "expirydate")
    private LocalDate expiryDate;

    @Column(name = "receiveddate")
    private LocalDate receivedDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private MaterialVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_user_id")
    private UserModel receivedByUser;

    @JsonIgnore
    @OneToMany(mappedBy = "inventory")
    private List<StockLots> stockLots;
}
