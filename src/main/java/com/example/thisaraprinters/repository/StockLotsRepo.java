package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.StockLots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockLotsRepo extends JpaRepository<StockLots, Integer> {
    List<StockLots> findByCreatedAtBetween(LocalDate start, LocalDate end);
    List<StockLots> findByVariantIdOrderByCreatedAtAsc(Integer variantId);
}
