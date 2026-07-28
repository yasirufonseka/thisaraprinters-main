package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.AddNewMaterialDto;
import com.example.thisaraprinters.dto.InventoryDto;
import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.Inventory;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.model.Materials;
import com.example.thisaraprinters.service.InventoryService;
import com.example.thisaraprinters.service.MaterialsService;
import com.example.thisaraprinters.service.SupplierService;
import com.example.thisaraprinters.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

  private final MaterialsService materialsService;
  private final InventoryService inventoryService;
  private final SupplierService supplierService;
  private final UserService userService;
  // private final com.example.thisaraprinters.repository.StockLotsRepo
  // stockLotsRepo;

  @Autowired
  public InventoryController(MaterialsService materialsService,
      InventoryService inventoryService,
      SupplierService supplierService,
      UserService userService
  // com.example.thisaraprinters.repository.StockLotsRepo stockLotsRepo
  ) {
    this.materialsService = materialsService;
    this.inventoryService = inventoryService;
    this.supplierService = supplierService;
    this.userService = userService;
    // this.stockLotsRepo = stockLotsRepo;
  }

  // Show inventory management page
  @GetMapping("/management")
  public ModelAndView showInventoryManagement() {
    ModelAndView mav = new ModelAndView();
    mav.setViewName("inventoryManagement");
    mav.addObject("variants", materialsService.getAllVariantMaterials());
    mav.addObject("materials", materialsService.getAllMaterials());
    mav.addObject("users", userService.getAllUsers());
    mav.addObject("grns", inventoryService.getAllGRNs());
    mav.addObject("stocklots", inventoryService.getAllStockLots());
    return mav;
  }

  @PostMapping("/save/material")
  public ResponseEntity<?> saveMaterial(@RequestBody AddNewMaterialDto material) {
    String resualt = materialsService.saveMaterial(material);

    return ResponseEntity.status(200).body(Map.of("message", resualt));
  }

  @PostMapping("/api/grn/save-full")
  public ResponseEntity<?> saveFullGrn(@RequestBody com.example.thisaraprinters.dto.InventoryDto grnData) {
    try {
      inventoryService.saveFullGrn(grnData);
      return ResponseEntity.status(200).body(Map.of("message", "Goods receipt note has been saved successfully."));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
    }
  }

  @GetMapping("/get/category")
  public ResponseEntity<List<Category>> getCategory() {
    List<Category> resualt = materialsService.getAllCategory();
    return ResponseEntity.status(200).body(resualt);
  }

  // ── Get all purchase orders for the GRN modal dropdown ──
  @GetMapping("/api/purchase-orders")
  public ResponseEntity<?> getPurchaseOrders() {
    try {
      List<java.util.Map<String, Object>> result = supplierService.getAllPurchaseOrders().stream()
          .map(po -> {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", po.getId());
            map.put("poNumber", po.getId() != null ? "PO-" + po.getId() : "");
            map.put("items", po.getItems());
            map.put("supplierId",   po.getSupplier() != null ? po.getSupplier().getId() : null);
            map.put("supplierName", po.getSupplier() != null ? po.getSupplier().getCompanyname() : "");
            return map;
          })
          .collect(java.util.stream.Collectors.toList());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
    }
  }

  @GetMapping("/get/materials")
  public ResponseEntity<List<Materials>> getMaterials() {
    List<Materials> resualt = materialsService.getAllMaterials();
    return ResponseEntity.status(200).body(resualt);
  }

  // ── Get GRN data by GRN (Inventory) ID (feeds edit modal) ──
  @GetMapping("/api/grn/{id}")
  public ResponseEntity<?> getGrnById(@PathVariable("id") Integer id) {
    try {
      return ResponseEntity.ok(inventoryService.getGrnById(id));
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
    }
  }

  // ── Get GRN data by StockLot ID (feeds edit modal) ──
  @GetMapping("/api/stocklot/{id}")
  public ResponseEntity<?> getGrnByStockLot(@PathVariable("id") Integer id) {
    try {
      return ResponseEntity.ok(inventoryService.getGrnByStockLotId(id));
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
    }
  }

  //  Update GRN + StockLot 
  @PutMapping("/api/stocklot/{id}/update")
  public ResponseEntity<?> updateGrn(@PathVariable("id") Integer id,
      @RequestBody com.example.thisaraprinters.dto.InventoryDto dto) {
    try {
      inventoryService.updateGrnByStockLotId(id, dto);
      return ResponseEntity.ok(Map.of("message", "GRN updated successfully."));
    } catch (Exception e) {
      return ResponseEntity.status(400).body(Map.of("message", "Error: " + e.getMessage()));
    }
  }

  // ── Delete StockLot + its linked GRN ──
  @DeleteMapping("/api/stocklot/{id}/delete")
  public ResponseEntity<?> deleteStockLot(@PathVariable("id") Integer id) {
    try {
      inventoryService.deleteByStockLotId(id);
      return ResponseEntity.ok(Map.of("message", "Record deleted successfully."));
    } catch (Exception e) {
      return ResponseEntity.status(400).body(Map.of("message", "Error: " + e.getMessage()));
    }
  }

  // ── Save Return Stock ──
  @PostMapping("/api/stocklot/return")
  public ResponseEntity<?> saveReturnStock(@RequestBody com.example.thisaraprinters.dto.ReturnStockDto dto) {
    try {
      inventoryService.saveReturnStock(dto);
      return ResponseEntity.ok(Map.of("message", "Return stock recorded successfully."));
    } catch (Exception e) {
      return ResponseEntity.status(400).body(Map.of("message", "Error: " + e.getMessage()));
    }
  }
}
