package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.InventoryDto;
import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MaterialRepo materialRepo;
    private final SupplierRepo supplierRepo;
    private final UserRepo userRepo;
    private final StockLotsRepo stockLotsRepo;
    private final com.example.thisaraprinters.repository.MaterialVariantRepo materialVariantRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                            MaterialRepo materialRepo,
                            SupplierRepo supplierRepo,
                            UserRepo userRepo,
                            com.example.thisaraprinters.repository.StockLotsRepo stockLotsRepo,
                            com.example.thisaraprinters.repository.MaterialVariantRepo materialVariantRepo,
                            PurchaseOrderRepo purchaseOrderRepo) {
        this.inventoryRepository = inventoryRepository;
        this.materialRepo = materialRepo;
        this.supplierRepo = supplierRepo;
        this.userRepo = userRepo;
        this.stockLotsRepo = stockLotsRepo;
        this.materialVariantRepo = materialVariantRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
    }

    public List<Inventory> getAllGRNs() {
        return inventoryRepository.findAll();
    }

    public List<StockLots> getAllStockLots() {
        return stockLotsRepo.findAll();
    }

    @Transactional
    public void saveFullGrn(InventoryDto dto) {

        // ── Issue 3 Fix: Validate all required inputs before touching the DB ──
        validateGrnInput(dto);

        Inventory inventory = new Inventory();

        inventory.setSupplierInvoiceNo(dto.getSupplierInvoiceNo());
        inventory.setBatchNo(dto.getBatchNo());
        inventory.setReceivedQuantity(dto.getRecivedquantity());
        inventory.setUnits(dto.getUnits());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setReceivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now());
        inventory.setNotes(dto.getNotes());

        // Set relationships
        if (dto.getVariant() != null && dto.getVariant().getId() != null) {
            com.example.thisaraprinters.model.MaterialVariant variant = materialVariantRepo.findById(dto.getVariant().getId()).orElse(null);
            inventory.setVariant(variant);
        }
        if (dto.getPurchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepo.findById(dto.getPurchaseOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found."));
            inventory.setPurchaseOrder(po);
        }
        if (dto.getReceivedByUser() != null && dto.getReceivedByUser().getId() != null) {
            inventory.setReceivedByUser(userRepo.findById(dto.getReceivedByUser().getId()).orElse(null));
        }

       
        //     build the GRN number from that ID 
        Inventory savedInventory = inventoryRepository.save(inventory);
        String grnNumber = "GRN-" + LocalDate.now().getYear() + "-" + String.format("%04d", savedInventory.getId());
        savedInventory.setGrnNumber(grnNumber);
        savedInventory = inventoryRepository.save(savedInventory);

        // Create Stock Lot entry
        if (savedInventory.getVariant() != null) {
            StockLots stockLot = new StockLots();
            stockLot.setInventory(savedInventory);
            stockLot.setVariant(savedInventory.getVariant());
            stockLot.setQuantity(savedInventory.getReceivedQuantity());
            stockLot.setSourceRef("GRN: " + savedInventory.getGrnNumber());
            stockLot.setLotType("GRN");
            stockLot.setCreatedAt(LocalDate.now());

            // Set dimensions and weight from variant
            stockLot.setWidth(savedInventory.getVariant().getWidth());
            stockLot.setHeight(savedInventory.getVariant().getHeight());
            stockLot.setWeight(savedInventory.getVariant().getWeightPerUnit());
            stockLot.setUnit(savedInventory.getVariant().getUnit());
            stockLot.setStatus("Available");

            stockLotsRepo.save(stockLot);
        }
    }

    // ── Validation helper ──
    private void validateGrnInput(InventoryDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("GRN data cannot be null.");
        }
        if (dto.getSupplierInvoiceNo() == null || dto.getSupplierInvoiceNo().isBlank()) {
            throw new IllegalArgumentException("Supplier Invoice Number is required.");
        }
        if (dto.getBatchNo() == null || dto.getBatchNo().isBlank()) {
            throw new IllegalArgumentException("Batch Number is required.");
        }
        if (dto.getRecivedquantity() == null || dto.getRecivedquantity() <= 0) {
            throw new IllegalArgumentException("Received quantity must be greater than zero.");
        }
        if (dto.getUnits() == null || dto.getUnits().isBlank()) {
            throw new IllegalArgumentException("Unit is required.");
        }
        if (dto.getVariant() == null || dto.getVariant().getId() == null) {
            throw new IllegalArgumentException("Material Variant must be selected.");
        }
        if (dto.getPurchaseOrderId() == null) {
            throw new IllegalArgumentException("A Placed Order (Purchase Order) must be selected.");
        }
        if (dto.getReceivedByUser() == null || dto.getReceivedByUser().getId() == null) {
            throw new IllegalArgumentException("Received By user must be selected.");
        }
    }

    // ── Get GRN data by StockLot ID (for edit modal) ──
    @Transactional
    public java.util.Map<String, Object> getGrnByStockLotId(Integer stockLotId) {
        StockLots stockLot = stockLotsRepo.findById(stockLotId)
                .orElseThrow(() -> new IllegalArgumentException("Stock lot not found: " + stockLotId));
        Inventory inv = stockLot.getInventory();
        if (inv == null) throw new IllegalArgumentException("No GRN linked to this stock lot.");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("stockLotId",       stockLot.getId());
        result.put("inventoryId",      inv.getId());
        result.put("grnNumber",        inv.getGrnNumber());
        result.put("supplierInvoiceNo", inv.getSupplierInvoiceNo());
        result.put("batchNo",          inv.getBatchNo());
        result.put("receivedQuantity", inv.getReceivedQuantity());
        result.put("units",            inv.getUnits());
        result.put("receivedDate",     inv.getReceivedDate() != null ? inv.getReceivedDate().toString() : null);
        result.put("expiryDate",       inv.getExpiryDate()  != null ? inv.getExpiryDate().toString()  : null);
        result.put("notes",            inv.getNotes());
        result.put("variantId",        inv.getVariant()       != null ? inv.getVariant().getId() : null);
        result.put("purchaseOrderId",  inv.getPurchaseOrder() != null ? inv.getPurchaseOrder().getId() : null);
        result.put("supplierId",       inv.getPurchaseOrder() != null && inv.getPurchaseOrder().getSupplier() != null
                                        ? inv.getPurchaseOrder().getSupplier().getId() : null);
        result.put("supplierName",     inv.getPurchaseOrder() != null && inv.getPurchaseOrder().getSupplier() != null
                                        ? inv.getPurchaseOrder().getSupplier().getCompanyname() : null);
        result.put("receivedByUserId", inv.getReceivedByUser() != null ? inv.getReceivedByUser().getId() : null);
        return result;
    }

    // ── Get GRN data by GRN (Inventory) ID (for edit modal) ──
    @Transactional
    public java.util.Map<String, Object> getGrnById(Integer grnId) {
        Inventory inv = inventoryRepository.findById(grnId)
                .orElseThrow(() -> new IllegalArgumentException("GRN not found: " + grnId));

        StockLots stockLot = (inv.getStockLots() != null && !inv.getStockLots().isEmpty())
                ? inv.getStockLots().get(0)
                : null;

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("stockLotId",       stockLot != null ? stockLot.getId() : null);
        result.put("inventoryId",      inv.getId());
        result.put("grnNumber",        inv.getGrnNumber());
        result.put("supplierInvoiceNo", inv.getSupplierInvoiceNo());
        result.put("batchNo",          inv.getBatchNo());
        result.put("receivedQuantity", inv.getReceivedQuantity());
        result.put("units",            inv.getUnits());
        result.put("receivedDate",     inv.getReceivedDate() != null ? inv.getReceivedDate().toString() : null);
        result.put("expiryDate",       inv.getExpiryDate()  != null ? inv.getExpiryDate().toString()  : null);
        result.put("notes",            inv.getNotes());
        result.put("variantId",        inv.getVariant()       != null ? inv.getVariant().getId() : null);
        result.put("purchaseOrderId",  inv.getPurchaseOrder() != null ? inv.getPurchaseOrder().getId() : null);
        result.put("supplierId",       inv.getPurchaseOrder() != null && inv.getPurchaseOrder().getSupplier() != null
                                        ? inv.getPurchaseOrder().getSupplier().getId() : null);
        result.put("supplierName",     inv.getPurchaseOrder() != null && inv.getPurchaseOrder().getSupplier() != null
                                        ? inv.getPurchaseOrder().getSupplier().getCompanyname() : null);
        result.put("receivedByUserId", inv.getReceivedByUser() != null ? inv.getReceivedByUser().getId() : null);
        return result;
    }

    // ── Update GRN and its linked StockLot ──
    @Transactional
    public void updateGrnByStockLotId(Integer stockLotId, InventoryDto dto) {
        validateGrnInput(dto);

        StockLots stockLot = stockLotsRepo.findById(stockLotId)
                .orElseThrow(() -> new IllegalArgumentException("Stock lot not found: " + stockLotId));
        Inventory inv = stockLot.getInventory();
        if (inv == null) throw new IllegalArgumentException("No GRN linked to this stock lot.");

        // Update inventory fields
        inv.setSupplierInvoiceNo(dto.getSupplierInvoiceNo());
        inv.setBatchNo(dto.getBatchNo());
        inv.setReceivedQuantity(dto.getRecivedquantity());
        inv.setUnits(dto.getUnits());
        inv.setExpiryDate(dto.getExpiryDate());
        inv.setReceivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now());
        inv.setNotes(dto.getNotes());

        if (dto.getVariant() != null && dto.getVariant().getId() != null) {
            com.example.thisaraprinters.model.MaterialVariant variant =
                    materialVariantRepo.findById(dto.getVariant().getId()).orElse(null);
            inv.setVariant(variant);
            // Keep stock lot dimensions in sync with variant
            if (variant != null) {
                stockLot.setVariant(variant);
                stockLot.setWidth(variant.getWidth());
                stockLot.setHeight(variant.getHeight());
                stockLot.setWeight(variant.getWeightPerUnit());
                stockLot.setUnit(variant.getUnit());
            }
        }
        if (dto.getPurchaseOrderId() != null) {
            PurchaseOrder po = purchaseOrderRepo.findById(dto.getPurchaseOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found."));
            inv.setPurchaseOrder(po);
        }
        if (dto.getReceivedByUser() != null && dto.getReceivedByUser().getId() != null) {
            inv.setReceivedByUser(userRepo.findById(dto.getReceivedByUser().getId()).orElse(null));
        }

        // Sync stock lot quantity
        stockLot.setQuantity(dto.getRecivedquantity());
        stockLot.setSourceRef("GRN: " + inv.getGrnNumber());

        inventoryRepository.save(inv);
        stockLotsRepo.save(stockLot);
    }

    // ── Delete StockLot and its linked Inventory record ──
    @Transactional
    public void deleteByStockLotId(Integer stockLotId) {
        StockLots stockLot = stockLotsRepo.findById(stockLotId)
                .orElseThrow(() -> new IllegalArgumentException("Stock lot not found: " + stockLotId));
        Inventory inv = stockLot.getInventory();

        stockLotsRepo.delete(stockLot);          // delete child first (FK constraint)
        if (inv != null) {
            inventoryRepository.delete(inv);     // then delete parent GRN
        }
    }

    // ── Save Returned Stock as a Stock Lot ──
    @Transactional
    public void saveReturnStock(com.example.thisaraprinters.dto.ReturnStockDto dto) {
        if (dto.getVariantId() == null) {
            throw new IllegalArgumentException("Material Variant must be selected.");
        }
        if (dto.getReturnedQty() == null || dto.getReturnedQty() <= 0) {
            throw new IllegalArgumentException("Returned quantity must be greater than zero.");
        }
        if (dto.getJobNo() == null || dto.getJobNo().isBlank()) {
            throw new IllegalArgumentException("Job Number is required.");
        }

        com.example.thisaraprinters.model.MaterialVariant variant = 
                materialVariantRepo.findById(dto.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with ID: " + dto.getVariantId()));

        StockLots stockLot = new StockLots();
        stockLot.setVariant(variant);
        stockLot.setQuantity(dto.getReturnedQty());
        stockLot.setWidth(dto.getWidth());
        stockLot.setHeight(dto.getHeight());
        stockLot.setWeight(dto.getWeight());
        stockLot.setUnit(dto.getUnit() != null ? dto.getUnit() : variant.getUnit());
        stockLot.setSourceRef("Job: " + dto.getJobNo().trim());
        stockLot.setLotType("Return");
        stockLot.setStatus("Available");
        stockLot.setCreatedAt(LocalDate.now());

        stockLotsRepo.save(stockLot);
    }

}


