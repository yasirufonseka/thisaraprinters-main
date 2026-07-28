package com.example.thisaraprinters.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "material_variant")
public class MaterialVariant {
      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Materials material;

    @Column(name = "gsm")
    private Integer gsm;

    @Column(name = "width_mm")
    private Double width;

    @Column(name = "height_mm")
    private Double height;

    @Column(name = "sheets_per_ream")
    private Integer sheetsPerReam;

    @Column(name = "weight_per_unit")
    private Double weightPerUnit;

    @Column(name = "unit")
    private String unit;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "status")
    private String status;

    @Transient
    private Double sheetRate;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "variant")
    private List<StockLots> stockLots;

}
