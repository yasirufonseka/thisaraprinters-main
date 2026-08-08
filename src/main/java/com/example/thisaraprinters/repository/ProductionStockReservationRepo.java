package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.ProductionStockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionStockReservationRepo extends JpaRepository<ProductionStockReservation, Long> {
    List<ProductionStockReservation> findByProductionOrderIdOrderById(String orderId);
    List<ProductionStockReservation> findByProductionOrderIdAndStockLotVariantIdOrderById(String orderId, Integer variantId);
}
