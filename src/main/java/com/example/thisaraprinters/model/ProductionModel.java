package com.example.thisaraprinters.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "production")
public class ProductionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "description")
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "priority")
    private String priority; // "Urgent", "High", "Normal"

    @Column(name = "status")
    private String status; // "New Orders", "Design Phase", "Printing", "Finishing", "Ready to Deliver", "Dispatched"

    @Column(name = "artwork_path")
    private String artworkPath; // server-side path to the uploaded design file

    @Column(name = "artwork_original_name")
    private String artworkOriginalName; // original file name shown to the user

    @Column(name = "date_sent_ to_ production")
    private LocalDate dateSentToProduction;


    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name ="quotations_id" , referencedColumnName = "id")
    private QuotationModel quotationid;
}

