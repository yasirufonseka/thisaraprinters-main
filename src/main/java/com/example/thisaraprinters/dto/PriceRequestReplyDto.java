package com.example.thisaraprinters.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PriceRequestReplyDto {
    private Double unitPrice;
    private Double deliveryCharge;
    private Double totalAmount;
    private Integer quantity;
    private LocalDate deliveryDate;
    private Integer priceRequestId;
    private Integer supplierId;
}
