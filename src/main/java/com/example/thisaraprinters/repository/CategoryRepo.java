package com.example.thisaraprinters.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thisaraprinters.model.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    
}
