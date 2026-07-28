package com.example.thisaraprinters.dto;

import lombok.Data;

@Data
public class ReturnStockDto {
    private Integer variantId;
    private Double height;
    private Double width;
    private Double weight;
    private String jobNo;
    private Integer returnedQty;
    private String unit;
}
