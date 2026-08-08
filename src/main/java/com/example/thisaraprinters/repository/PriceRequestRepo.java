package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.PriceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRequestRepo extends JpaRepository<PriceRequest, Integer> {
    boolean existsBySupplierlist_Id(Integer supplierId);

}
