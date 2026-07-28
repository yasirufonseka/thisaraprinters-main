package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.CustomerModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<CustomerModel, Integer> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    boolean existsByNameAndIdNot(String name, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByPhoneAndIdNot(String phone, Integer id);
}

