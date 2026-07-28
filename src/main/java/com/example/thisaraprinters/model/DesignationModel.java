package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "designation")
public class DesignationModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;
    private String designation;
}
 