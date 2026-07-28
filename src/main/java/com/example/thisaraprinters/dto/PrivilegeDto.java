package com.example.thisaraprinters.dto;

import lombok.Data;

@Data
public class PrivilegeDto {

    private Integer id;
    private String module;
    private Boolean canInsert;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canView;
}
