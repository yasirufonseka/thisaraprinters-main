package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "pricerequestreply")
@Data
public class PriceRequestReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "unitprice")
    private Double unitPrice;

    @Column(name = "deliverycharge")
    private Double deliveryCharge;

    @Column(name = "totalamount")
    private Double totalAmount;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "deliverydate")
    private LocalDate deliveryDate;

    @Column(name = "replydate")
    private LocalDate replyDate;

    @ManyToOne
    @JoinColumn(name = "pricerequest_id")
    private PriceRequest priceRequest;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}
