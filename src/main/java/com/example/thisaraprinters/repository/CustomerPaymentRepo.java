package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPaymentRepo extends JpaRepository<CustomerPayment, Integer> {

    List<CustomerPayment> findByCustomer_Id(Integer customerId);

    Optional<CustomerPayment> findByQuotation_Id(Integer quotationId);

    boolean existsByQuotation_Id(Integer quotationId);

    Optional<CustomerPayment> findByProduction_Id(Integer productionId);

    boolean existsByProduction_Id(Integer productionId);
}
