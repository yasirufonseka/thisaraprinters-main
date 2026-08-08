package com.example.thisaraprinters.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MaterialUsageDto {
    private Integer variantId;
    private Integer quantityUsed;
    private String jobNo;
    private String purpose;
    private LocalDate dateUsed;
}
