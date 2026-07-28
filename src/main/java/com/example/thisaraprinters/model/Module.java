package com.example.thisaraprinters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "module")
public class Module {
    @Id
    @Column(name = "id")
    private int id;
    
    @Column(name = "name")
    private String name;
}
