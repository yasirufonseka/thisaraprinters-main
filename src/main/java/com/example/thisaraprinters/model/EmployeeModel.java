package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "employee")
public class EmployeeModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    
    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;
    
    @Column(name = "fullname")
    private String fullname;
    
    @Column(name = "callingname")
    private String callingname;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "dob")
    private LocalDate dob;
    
    @Column(name = "nic")
    private String nic;
    
    @Column(name = "phonenumber")
    private String phonenumber;
    
    @Column(name = "emgpersonname")
    private String emgpersonname;
    
    @Column(name = "emgpersonphonenumber")
    private String emgpersonphonenumber;
    
    @Column(name = "addeddate")
    private LocalDate addeddate;
    
    @Column(name = "updateddate")
    private LocalDate updateddate;
    @ManyToOne
    @JoinColumn(name = "designation_id", referencedColumnName = "id")
    private DesignationModel designationid;
}
