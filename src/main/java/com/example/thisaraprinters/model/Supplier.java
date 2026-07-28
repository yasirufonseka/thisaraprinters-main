package com.example.thisaraprinters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "suppliers")
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "companyname")
    private String companyname;
    
    @Column(name = "contactperson")
    private String contactperson;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "contact")
    private String contact;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "description")
    private String description;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "suppliers_has_materials",
            joinColumns = @JoinColumn(name = "suppliers_id"),
            inverseJoinColumns = @JoinColumn(name = "materials_id")

    )
    private List<Materials> materials = new ArrayList<>();
    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "supplierlist")
    private List<PriceRequest> priceRequests;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "suppliers_has_category",
            joinColumns = @JoinColumn(name = "suppliers_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")

    )
    private List<Category> category;
}
