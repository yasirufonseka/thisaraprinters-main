package com.example.thisaraprinters.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PurchaseOrderDto {
    private Integer id;
    private Integer supplierId;
    private Integer priceRequestId;
    private LocalDate orderDate;
    private String items;
    private String quantity;
    private Double totalAmount;
    private Double paidAmount;
    private String paymentStatus;
    private String notes;
}
