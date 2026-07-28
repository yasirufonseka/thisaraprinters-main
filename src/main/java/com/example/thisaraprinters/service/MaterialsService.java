package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.AddNewMaterialDto;
import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.model.Materials;
import com.example.thisaraprinters.repository.CategoryRepo;
import com.example.thisaraprinters.repository.MaterialRepo;
import com.example.thisaraprinters.repository.MaterialVariantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialsService {

    private final MaterialRepo materialRepo;
    private final CategoryRepo categoryRepo;
    private final MaterialVariantRepo materialVariantRepo;

    @Autowired
    public MaterialsService(MaterialRepo materialRepo,
                            CategoryRepo categoryRepo,
                            MaterialVariantRepo materialVariantRepo) {

        this.materialRepo = materialRepo;
        this.categoryRepo = categoryRepo;
        this.materialVariantRepo = materialVariantRepo;
    }

    // Get all materials
    public List<MaterialVariant> getAllVariantMaterials() {
        return materialVariantRepo.findAll();
    }

    public List<Materials> getAllMaterials() {
        return materialRepo.findAll();
    }

    // Get material by ID
    public Optional<Materials> getMaterialById(Integer id) {
        return materialRepo.findById(id);
    }

    // Add new material variant
    public String saveMaterial(AddNewMaterialDto material) {

        if (material.getMaterialName() == null || material.getMaterialName().getId() == null) {
            throw new IllegalArgumentException("MaterialName or ID is null");
        }

        Optional<Materials> materialOpt = materialRepo.findById(material.getMaterialName().getId());
        if (materialOpt.isEmpty()) {
            throw new IllegalArgumentException("Material not found");
        }

        // Check if variant already exists
        List<MaterialVariant> existingVariants = materialVariantRepo.findByMaterialId(material.getMaterialName().getId());
        for (MaterialVariant existingVariant : existingVariants) {
            if (existingVariant.getGsm() != null && existingVariant.getGsm().equals(material.getMaterialgsm())
                    && existingVariant.getHeight() != null && existingVariant.getHeight().equals(material.getHightofpaper())
                    && existingVariant.getWidth() != null && existingVariant.getWidth().equals(material.getWidthtofpaper())) {
                throw new DataIntegrityViolationException("Material variant already exists with same specifications");
            }
        }

        MaterialVariant saveMaterial = new MaterialVariant();
        saveMaterial.setMaterial(materialOpt.get());
        saveMaterial.setGsm(material.getMaterialgsm());
        saveMaterial.setHeight(material.getHightofpaper());
        saveMaterial.setWidth(material.getWidthtofpaper());
        saveMaterial.setSheetsPerReam(material.getSheetperream());
        saveMaterial.setWeightPerUnit(material.getWeight());
        saveMaterial.setUnit(material.getUnit());
        saveMaterial.setReorderLevel(material.getReorderlevel());
        saveMaterial.setStatus("Sufficient");

        materialVariantRepo.save(saveMaterial);

        return "Material variant added successfully";
    }

    // Update material
    public String updateMaterial(Integer id, Materials material) {
        try {
//            Optional<Materials> existingMaterial = materialRepo.findById(id);
//
//            if (existingMaterial.isEmpty()) {
//                return "Material not found";
//            }
//
//            Materials materialToUpdate = existingMaterial.get();
//
//            if (material.getMaterial() != null && !material.getMaterial().isEmpty()) {
//                materialToUpdate.setMaterial(material.getMaterial());
//            }
//            if (material.getAvailablequantity() != null) {
//                materialToUpdate.setAvailablequantity(material.getAvailablequantity());
//            }
//            if (material.getUnits() != null && !material.getUnits().isEmpty()) {
//                materialToUpdate.setUnits(material.getUnits());
//            }
//            if (material.getReorderlevel() != null) {
//                materialToUpdate.setReorderlevel(material.getReorderlevel());
//            }
//            if (material.getStatus() != null && !material.getStatus().isEmpty()) {
//                materialToUpdate.setStatus(material.getStatus());
//            }

           // materialRepo.save(materialToUpdate);
            return "Material updated successfully";
        } catch (Exception e) {
            return "Error updating material: " + e.getMessage();
        }
    }

    // Delete material
    public String deleteMaterial(Integer id) {
        try {
            Optional<Materials> material = materialRepo.findById(id);
            
            if (material.isEmpty()) {
                return "Material not found";
            }

            materialRepo.deleteById(id);
            return "Material deleted successfully";
        } catch (Exception e) {
            return "Error deleting material: " + e.getMessage();
        }
    }

    // Record material usage
    public String recordMaterialUsage(Integer materialId, Integer quantityUsed) {
        try {
            if (quantityUsed == null || quantityUsed <= 0) {
                return "Quantity used must be greater than 0";
            }

            Optional<Materials> materialOpt = materialRepo.findById(materialId);

            if (materialOpt.isEmpty()) {
                return "Material not found";
            }

            Materials mat = materialOpt.get();
            //Integer currentQuantity = mat.getAvailablequantity() == null ? 0 : mat.getAvailablequantity();

//            if (currentQuantity < quantityUsed) {
//                return "Insufficient quantity. Available: " + currentQuantity + ", Required: " + quantityUsed;
//            }

            //  mat.setAvailablequantity(currentQuantity - quantityUsed);
            updateMaterialStatus(mat);

            materialRepo.save(mat);
            return "Material usage recorded successfully";
        } catch (Exception e) {
            return "Error recording material usage: " + e.getMessage();
        }
    }

    private void updateMaterialStatus(Materials material) {
      //  int availableQuantity = material.getAvailablequantity() == null ? 0 : material.getAvailablequantity();
      //  int reorderLevel = material.getReorderlevel() == null ? 0 : material.getReorderlevel();

//        if (availableQuantity == 0) {
//            material.setStatus("Out of Stock");
//        } else if (availableQuantity < reorderLevel) {
//            material.setStatus("Low Stock");
//        } else {
//            material.setStatus("Sufficient");
//        }
    }

    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }
}