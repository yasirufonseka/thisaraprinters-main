package com.example.thisaraprinters.dto;

import com.example.thisaraprinters.model.Supplier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
public class PriceRequestDto {

    private String materialcategory ;
    private String itemSpecification ;
    private String quantity ;
    private LocalDate deadline ;
    private String message;
    private LocalDate createDate;
    private String requeststatus;
    private List<Supplier> supplierlist;
}
