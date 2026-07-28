package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.Supplier;
import com.example.thisaraprinters.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepo extends JpaRepository<Supplier, Integer> {

}
