package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "production_stock_reservation")
public class ProductionStockReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_id")
    private ProductionModel production;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_lot_id")
    private StockLots stockLot;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "used_quantity", nullable = false)
    private Integer usedQuantity = 0;
}
