package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "stock_lots")
public class StockLots {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private MaterialVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "width_mm")
    private Double width;

    @Column(name = "height_mm")
    private Double height;

    @Column(name = "weight_kg")
    private Double weight;

    @Column(name = "source_ref")
    private String sourceRef;

    @Column(name = "lot_type")
    private String lotType;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "unit")
    private String unit;

    @Column(name = "status")
    private String status;


}
