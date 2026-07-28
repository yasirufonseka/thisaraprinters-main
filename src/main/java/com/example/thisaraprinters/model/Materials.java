package com.example.thisaraprinters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materials")
@Data
public class Materials {
       @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "material")
    private String material;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<MaterialVariant> variants;

}
