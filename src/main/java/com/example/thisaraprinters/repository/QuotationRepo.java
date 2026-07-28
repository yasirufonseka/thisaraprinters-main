package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.QuotationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface QuotationRepo extends JpaRepository<QuotationModel, Integer> {

    @Query("SELECT COALESCE(SUM(q.quotationamount), 0) FROM QuotationModel q")
    double sumTotalRevenue();

    List<QuotationModel> findByQuotationdateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(q.quotationamount), 0) FROM QuotationModel q WHERE q.quotationdate BETWEEN :start AND :end")
    double sumRevenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
