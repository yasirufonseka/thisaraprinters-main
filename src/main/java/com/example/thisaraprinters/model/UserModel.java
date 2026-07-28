package com.example.thisaraprinters.model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;
    
    @JsonIgnore()
    @Column(name = "password")
    private String password;
    
    @Column(name = "addeddate")
    private LocalDate addeddate;
    
    @Column(name = "updateddate")
    private LocalDate updateddate;
    
    @Column(name = "note")
    private String note;
    
    @Column(name = "userphoto")
    private String userphoto;
    
    @Column(name = "status")
    private String status;
    @OneToOne()
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private EmployeeModel employeeid;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RoleModel role;

}
