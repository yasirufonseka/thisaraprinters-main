package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPaymentRepo extends JpaRepository<SupplierPayment, Integer> {
    Optional<SupplierPayment> findTopByPurchaseOrder_IdOrderByCreatedAtDesc(Integer purchaseOrderId);

    List<SupplierPayment> findByPurchaseOrder_Id(Integer purchaseOrderId);
}
