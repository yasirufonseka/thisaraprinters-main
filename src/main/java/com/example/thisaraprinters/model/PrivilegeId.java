package com.example.thisaraprinters.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeId implements Serializable {
    private Integer role;   // matches type of RoleModel's PK (Integer) and property name in PrivilegeModel
    private Integer module; // matches type of Module's PK (int/Integer) and property name in PrivilegeModel
}
