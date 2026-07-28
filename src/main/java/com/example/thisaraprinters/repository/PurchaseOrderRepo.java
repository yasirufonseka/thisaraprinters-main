package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Integer> {
    List<PurchaseOrder> findByOrderDateBetween(LocalDate start, LocalDate end);
    List<PurchaseOrder> findByCreatedDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT o FROM PurchaseOrder o WHERE " +
           "(o.orderDate IS NOT NULL AND o.orderDate BETWEEN :start AND :end) OR " +
           "(o.orderDate IS NULL AND o.createdDate BETWEEN :start AND :end)")
    List<PurchaseOrder> findByDateRangeSmart(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
