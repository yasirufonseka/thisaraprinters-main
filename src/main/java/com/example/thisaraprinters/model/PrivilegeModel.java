package com.example.thisaraprinters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "role_has_module_privilege")
@IdClass(PrivilegeId.class)
public class PrivilegeModel {

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    private RoleModel role;

    @Id
    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @Column(name = "canview")
    private Boolean canView = false;

    @Column(name = "caninsert")
    private Boolean canInsert = false;

    @Column(name = "canupdate")
    private Boolean canUpdate = false;

    @Column(name = "candelete")
    private Boolean canDelete = false;
}
