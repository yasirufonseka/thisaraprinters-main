package com.example.thisaraprinters.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProductionDto {
    private int id;
    private String orderId;
    private String customerName;
    private String description;
    private LocalDate deadline;
    private String priority;
    private String status;
}
