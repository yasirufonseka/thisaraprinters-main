package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query(value = "SELECT grnnumber FROM inventory WHERE grnnumber LIKE :yearPattern ORDER BY id DESC LIMIT 1", nativeQuery = true)
    String findLastGrnNumberForYear(@Param("yearPattern") String yearPattern);

    List<Inventory> findByReceivedDateBetween(LocalDate start, LocalDate end);
}
