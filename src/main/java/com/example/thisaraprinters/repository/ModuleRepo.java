package com.example.thisaraprinters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.thisaraprinters.model.Module;

public interface ModuleRepo extends JpaRepository<Module,Integer> {
    Module findByName(String name);
}
