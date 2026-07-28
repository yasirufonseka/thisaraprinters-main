package com.example.thisaraprinters.dto;

import lombok.Data;

@Data
public class CustomerPaymentDto {

    private Integer quotationId;
    private Integer productionId;
    private Integer customerId;
    private String paymentStatus;
    private String paymentMethod;
    private Double paidAmount;
    private String referenceNo;
    private String paymentNotes;
}
