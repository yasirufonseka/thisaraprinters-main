package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "quotations")
public class QuotationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String productsize;
    private Integer quantity;
    private String color;
    private String quotationdescription;
    private String cuttingtype;
    private String foiling;
    private String lamination;
    private String bindingtype;
    private String papertype;
    private double quotationamount;
    private double advanceamount;
    private LocalDate quotationdate;
    private String quotationstatus;
    @Column(columnDefinition = "integer default 0")
    private Integer quotationvalidity = 0;
    private LocalDate expiryDate;

    @Column(name = "wastage_sheets")
    private Integer wastageSheets;

    @Column(name = "edge_margin_mm")
    private Double edgeMarginMm;

    @Column(name = "gutter_mm")
    private Double gutterMm;

    @Column(name = "paper_rate_per_sheet")
    private double paperRatePerSheet;

    @Column(name = "paper_cost")
    private double paperCost;

    @Column(name = "finishing_cost")
    private double finishingCost;

    @Column(name = "impression_cost")
    private double impressionCost;

    @Column(name = "service_charge_pct")
    private double serviceChargePercentage;

    @Column(name = "service_charge_amount")
    private double serviceChargeAmount;

    @Column(name = "unit_price")
    private double unitPrice;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "customersid")
    private CustomerModel customer;

    @ToString.Exclude
    @ManyToMany()
    @JoinTable(
            name = "_quotations_has_material_variants",
            joinColumns = @JoinColumn(name = "quotations_id"),
            inverseJoinColumns = @JoinColumn(name = "variant_id")
    )
    private List<MaterialVariant> materialsList;
}

