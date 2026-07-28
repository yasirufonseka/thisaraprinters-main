package com.example.thisaraprinters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.thisaraprinters.model.RoleModel;



public interface RoleRepo extends JpaRepository<RoleModel, Integer> {


}
