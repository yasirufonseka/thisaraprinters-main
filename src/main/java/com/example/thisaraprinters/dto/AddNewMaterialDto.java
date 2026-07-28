package com.example.thisaraprinters.dto;

import com.example.thisaraprinters.model.Materials;
import lombok.Data;

@Data
public class AddNewMaterialDto {
    private Materials materialName;
    private Integer materialgsm;
    private Integer sheetperream;
    private Integer reorderlevel;
    private Double hightofpaper;
    private Double widthtofpaper;
    private Double weight;
    private String unit;
}
