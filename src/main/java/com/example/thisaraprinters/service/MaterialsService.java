package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.AddNewMaterialDto;
import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.model.Materials;
import com.example.thisaraprinters.repository.CategoryRepo;
import com.example.thisaraprinters.repository.MaterialRepo;
import com.example.thisaraprinters.repository.MaterialVariantRepo;

import jakarta.transaction.Transactional;

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

        Materials selectedMaterial = resolveMaterial(material);

        // Check if variant already exists
        List<MaterialVariant> existingVariants = materialVariantRepo.findByMaterialId(selectedMaterial.getId());
        for (MaterialVariant existingVariant : existingVariants) {
            if (existingVariant.getGsm() != null && existingVariant.getGsm().equals(material.getMaterialgsm())
                    && existingVariant.getHeight() != null && existingVariant.getHeight().equals(material.getHightMm())
                    && existingVariant.getWidth() != null
                    && existingVariant.getWidth().equals(material.getWidthtMm())) {
                throw new DataIntegrityViolationException("Material variant already exists with same specifications");
            }
        }
        System.out.println(material.getHightMm());

        inchesToMmConverter(material);

        MaterialVariant saveMaterial = new MaterialVariant();
        saveMaterial.setMaterial(selectedMaterial);
        saveMaterial.setGsm(material.getMaterialgsm());
        saveMaterial.setHeight(material.getHightMm());
        saveMaterial.setWidth(material.getWidthtMm());
        saveMaterial.setSheetsPerReam(material.getSheetperream());
        saveMaterial.setWeightPerUnit(material.getWeight());
        saveMaterial.setUnit(material.getUnit());
        saveMaterial.setReorderLevel(material.getReorderlevel());
        saveMaterial.setPartNumber(partNumberGenerator(material, selectedMaterial));
        saveMaterial.setStatus("Sufficient");

        materialVariantRepo.save(saveMaterial);

        return "Material variant added successfully";
    }

    private Materials resolveMaterial(AddNewMaterialDto material) {

        if (material.getMaterialName() != null && material.getMaterialName().getId() != null) {
            return materialRepo.findById(material.getMaterialName().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found"));
        }

        String newMaterialName = material.getNewMaterialName() == null ? "" : material.getNewMaterialName().trim();
        if (newMaterialName.isEmpty()) {
            throw new IllegalArgumentException("Material name is required");
        }

        if (material.getCategory() == null || material.getCategory().getId() == null) {
            throw new IllegalArgumentException("Material category is required");
        }

        Category category = categoryRepo.findById(material.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Material category not found"));

        Optional<Materials> existingMaterial = materialRepo.findByMaterialIgnoreCaseAndCategoryId(newMaterialName,
                category.getId());
        if (existingMaterial.isPresent()) {
            return existingMaterial.get();
        }

        Materials createdMaterial = new Materials();
        createdMaterial.setMaterial(newMaterialName);
        createdMaterial.setCategory(category);
        createdMaterial.setStatus("Active");
        return materialRepo.save(createdMaterial);
    }

    // Update material
    public String updateMaterial(Integer id, Materials material) {
        try {
            // Optional<Materials> existingMaterial = materialRepo.findById(id);
            //
            // if (existingMaterial.isEmpty()) {
            // return "Material not found";
            // }
            //
            // Materials materialToUpdate = existingMaterial.get();
            //
            // if (material.getMaterial() != null && !material.getMaterial().isEmpty()) {
            // materialToUpdate.setMaterial(material.getMaterial());
            // }
            // if (material.getAvailablequantity() != null) {
            // materialToUpdate.setAvailablequantity(material.getAvailablequantity());
            // }
            // if (material.getUnits() != null && !material.getUnits().isEmpty()) {
            // materialToUpdate.setUnits(material.getUnits());
            // }
            // if (material.getReorderlevel() != null) {
            // materialToUpdate.setReorderlevel(material.getReorderlevel());
            // }
            // if (material.getStatus() != null && !material.getStatus().isEmpty()) {
            // materialToUpdate.setStatus(material.getStatus());
            // }

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
            // Integer currentQuantity = mat.getAvailablequantity() == null ? 0 :
            // mat.getAvailablequantity();

            // if (currentQuantity < quantityUsed) {
            // return "Insufficient quantity. Available: " + currentQuantity + ", Required:
            // " + quantityUsed;
            // }

            // mat.setAvailablequantity(currentQuantity - quantityUsed);
            updateMaterialStatus(mat);

            materialRepo.save(mat);
            return "Material usage recorded successfully";
        } catch (Exception e) {
            return "Error recording material usage: " + e.getMessage();
        }
    }

    private void updateMaterialStatus(Materials material) {
        // int availableQuantity = material.getAvailablequantity() == null ? 0 :
        // material.getAvailablequantity();
        // int reorderLevel = material.getReorderlevel() == null ? 0 :
        // material.getReorderlevel();

        // if (availableQuantity == 0) {
        // material.setStatus("Out of Stock");
        // } else if (availableQuantity < reorderLevel) {
        // material.setStatus("Low Stock");
        // } else {
        // material.setStatus("Sufficient");
        // }
    }

    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }


    private String partNumberGenerator(AddNewMaterialDto material, Materials selectedMaterial) {

        String partNumber = "";

        if (material.getMaterialgsm() == null) {
            throw new IllegalArgumentException("GSM is required for part number generation");
        }
        if (selectedMaterial == null) {
            throw new IllegalArgumentException("Material name is required for part number generation");
        }

        String materialName = selectedMaterial.getMaterial();
        String categoryName = selectedMaterial.getCategory() != null ? selectedMaterial.getCategory().getName() : null;
        if (categoryName == null) {
            throw new IllegalArgumentException("Category name is required for part number generation");
        }

        switch (categoryName) {
            case "Ink":
                partNumber = materialName.replaceAll("\\s+", "").trim().substring(0, Math.min(5, materialName.replaceAll("\\s+", "").length())).toUpperCase() + "-" + material.getMaterialgsm();
                break;

            case "Paper":
                if (material.getMaterialName() == null && material.getNewMaterialName() != null) {
                    String nm = material.getNewMaterialName().replaceAll("\\s+", "");
                    partNumber = nm.substring(0, Math.min(5, nm.length())).toUpperCase() + "-" + material.getMaterialgsm();
                } else {
                    String nm = materialName.replaceAll("\\s+", "");
                    partNumber = nm.substring(0, Math.min(5, nm.length())).toUpperCase() + "-" + material.getMaterialgsm();
                }
                break;

            case "Plate":
                if (material.getNewMaterialName() == null) {
                    String nm = materialName.replaceAll("\\s+", "");
                    partNumber = "PLATE-" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                } else {
                    String nm = material.getNewMaterialName();
                    partNumber = "PLATE" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                }
                break;

            case "Blanket":
                if (material.getNewMaterialName() == null) {
                    String nm = materialName.replaceAll("\\s+", "");
                    partNumber = "BLANKET-" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                } else {
                    String nm = material.getNewMaterialName();
                    partNumber = "BLANKET" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                }
                break;

            case "Chemical Solution":
                if (material.getNewMaterialName() == null) {
                    String nm = materialName.replaceAll("\\s+", "");
                    partNumber = "CHE-" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                } else {
                    String nm = material.getNewMaterialName();
                    partNumber = "CHE" + nm.substring(0, Math.min(5, nm.length())).toUpperCase();
                }
                break;

            default:
                String nm = materialName.replaceAll("\\s+", "");
                partNumber = nm.substring(0, Math.min(5, nm.length())).toUpperCase() + "-" + material.getMaterialgsm();
                break;
        }
        return partNumber;
    }  


    private void inchesToMmConverter(AddNewMaterialDto variant) {
        if (variant.getHightMm() == null && variant.getWidthtMm() == null) {
            throw new  IllegalArgumentException("Hight and Width are mandatory");
        }
         variant.setWidthtMm(variant.getWidthtMm() * 25.4);
         variant.setHightMm(variant.getHightMm() * 25.4);

    }
}
