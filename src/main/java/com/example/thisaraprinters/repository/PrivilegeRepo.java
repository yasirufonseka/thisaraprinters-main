package com.example.thisaraprinters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.thisaraprinters.model.PrivilegeModel;
import com.example.thisaraprinters.model.PrivilegeId;

public interface PrivilegeRepo extends JpaRepository<PrivilegeModel, PrivilegeId> {
}
