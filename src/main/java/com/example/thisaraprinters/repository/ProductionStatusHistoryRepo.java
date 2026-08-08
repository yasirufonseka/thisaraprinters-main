package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.ProductionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionStatusHistoryRepo extends JpaRepository<ProductionStatusHistory, Long> { }
