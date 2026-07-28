package com.example.thisaraprinters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pricerequest")
@Data
public class PriceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id ;
    
    @Column(name = "materialcategory")
    private String materialcategory ;
    
    @Column(name = "itemspecification")
    private String itemSpecification ;
    
    @Column(name = "quantity")
    private String quantity ;
    
    @Column(name = "deadline")
    private LocalDate deadline ;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "createdate")
    private LocalDate createDate;
    
    @Column(name = "requeststatus")
    private String requeststatus;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "price_request_has_suppliers",
            joinColumns = @JoinColumn(name = "price_request_id"),
            inverseJoinColumns = @JoinColumn(name = "suppliers_id")

    )
    private List<Supplier> supplierlist;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "priceRequest")
    private List<PriceRequestReply> replies;
}
