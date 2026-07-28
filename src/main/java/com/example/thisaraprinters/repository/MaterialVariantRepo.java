package com.example.thisaraprinters.repository;

import com.example.thisaraprinters.model.MaterialVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface MaterialVariantRepo extends JpaRepository<MaterialVariant,Integer> {

   
    List<MaterialVariant> findByMaterialId(Integer materialId);
   // boolean findByHeightAndWidth(double height, double width);

  //  Optional<MaterialVariant> findByGsm(int gsm);
}
