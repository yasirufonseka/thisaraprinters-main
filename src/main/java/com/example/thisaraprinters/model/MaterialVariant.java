package com.example.thisaraprinters.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Materials material;

    @NotNull
    @Column(name = "gsm")
    private Integer gsm;

    @NotNull
    @Column(name = "width_mm", columnDefinition = "DOUBLE")
    private Double width;

    @NotNull
    @Column(name = "height_mm", columnDefinition = "DOUBLE")
    private Double height;

    @Column(name = "sheets_per_ream")
    private Integer sheetsPerReam;

    @NotNull
    @Column(name = "weight_per_unit", columnDefinition = "DOUBLE")
    private Double weightPerUnit;

    @Column(name = "unit")
    private String unit;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "status")
    private String status;

    @Column(name = "part_number")
    private String partNumber;

    // Kept explicitly because the save flow calls this setter at runtime.
    // Lombok's @Data generates the remaining accessors.
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    @Transient
    private Double sheetRate;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "variant")
    private List<StockLots> stockLots;

}
