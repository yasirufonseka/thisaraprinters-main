package com.example.thisaraprinters.dto;

import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.Materials;
import lombok.Data;

@Data
public class AddNewMaterialDto {
    private Category category;
    private Materials materialName;
    private String newMaterialName;
    private Integer materialgsm;
    private Integer sheetperream;
    private Integer reorderlevel;
    private Double hightMm;
    private Double widthtMm;
    private Double weight;
    private String unit;
    private String partNumber;
}
