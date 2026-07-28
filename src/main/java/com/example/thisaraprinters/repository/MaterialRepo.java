package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.Materials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepo extends JpaRepository<Materials,Integer> {

//    List<Materials> findAllById(List<Materials> materiels);
}
