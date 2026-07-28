package com.example.thisaraprinters.dto;

import com.example.thisaraprinters.model.CustomerModel;
import com.example.thisaraprinters.model.MaterialVariant;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class QuotationDto {
    private int id;
    private String productsize;
    private Integer quantity;
    private String color;
    private String quotationdescription;
    private String cuttingtype;
    private String foiling;
    private String lamination;
    private String bindingtype;
    private double quotationamount;
    private double advanceamount;
    private LocalDate quotationdate;
    private String quotationstatus;
    private Integer quotationvalidity;
    private LocalDate expiryDate;
    private LocalDate addeddate;
    private CustomerModel customer;
    private List<MaterialVariant> materialsList;

    // Cost Breakdown Fields
    private double paperRatePerSheet;
    private double paperCost;
    private double finishingCost;
    private double impressionCost;
    private Integer wastageSheets;
    private Double edgeMarginMm;
    private Double gutterMm;
    private double serviceChargePercentage;
    private double serviceChargeAmount;
    private double unitPrice;
}

