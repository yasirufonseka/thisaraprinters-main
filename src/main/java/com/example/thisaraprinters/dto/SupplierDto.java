package com.example.thisaraprinters.dto;

import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.Materials;

import lombok.Data;

import java.util.List;

@Data
public class SupplierDto {
    private String companyname;
    private String contactperson;
    private String email;
    private String contact;
    private String address;
    private String status;
    private String description;
    private List<Materials> materiels;
    private List<Category> category;
}
