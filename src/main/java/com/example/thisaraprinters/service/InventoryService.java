package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.InventoryDto;
import com.example.thisaraprinters.dto.MaterialUsageDto;
import com.example.thisaraprinters.model.*;
import com.example.thisaraprinters.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ProductionStockReservationRepo reservationRepo;
    private final ProductionRepo productionRepo;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                            MaterialRepo materialRepo,
                            SupplierRepo supplierRepo,
                            UserRepo userRepo,
                            com.example.thisaraprinters.repository.StockLotsRepo stockLotsRepo,
                            com.example.thisaraprinters.repository.MaterialVariantRepo materialVariantRepo,
                            PurchaseOrderRepo purchaseOrderRepo, ProductionStockReservationRepo reservationRepo,
                            ProductionRepo productionRepo) {
        this.inventoryRepository = inventoryRepository;
        this.materialRepo = materialRepo;
        this.supplierRepo = supplierRepo;
        this.userRepo = userRepo;
        this.stockLotsRepo = stockLotsRepo;
        this.materialVariantRepo = materialVariantRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.reservationRepo = reservationRepo;
        this.productionRepo = productionRepo;
    }
    //get save all GRNs
    public List<Inventory> getAllGRNs() {
        return inventoryRepository.findAll();
    }
    //Get all the Stock
    public List<StockLots> getAllStockLots() {
        return stockLotsRepo.findAll();
    }

    //save GRN
    @Transactional
    public void saveFullGrn(InventoryDto dto) {
       // StockLots stockLots = stockLotsRepo.findById(dto.setPurchaseOrderId());
        //  Validate all required inputs before touching the DB 
        validateGrnInput(dto);

        Inventory inventory = new Inventory();

        int quantity = dto.getRecivedquantity() ;

        inventory.setSupplierInvoiceNo(dto.getSupplierInvoiceNo());
        inventory.setBatchNo(dto.getBatchNo());
        inventory.setReceivedQuantity(dto.getRecivedquantity());
        inventory.setUnits(dto.getUnits());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setReceivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now());
        inventory.setNotes(dto.getNotes());

        // Set relationships
        if (dto.getVariant() != null && dto.getVariant().getId() != null) {
           MaterialVariant variant = materialVariantRepo.findById(dto.getVariant().getId()).orElse(null);
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

    //  Validation helper 
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

    //  Get GRN data by StockLot ID (for edit modal) 
    @Transactional
    public Map<String, Object> getGrnByStockLotId(Integer stockLotId) {
        StockLots stockLot = stockLotsRepo.findById(stockLotId)
                .orElseThrow(() -> new IllegalArgumentException("Stock lot not found: " + stockLotId));
        Inventory inv = stockLot.getInventory();
        if (inv == null) throw new IllegalArgumentException("No GRN linked to this stock lot.");

        Map<String, Object> result = new HashMap<>();
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
        result.put("materialName",     inv.getVariant() != null ? (inv.getVariant().getMaterial().getMaterial() + " (GSM: " + inv.getVariant().getGsm() + ")") : "-");
        result.put("receivedByUsername", inv.getReceivedByUser() != null ? inv.getReceivedByUser().getUsername() : "-");
        return result;
    }

    //  Get GRN data by Inventory ID for edit modal
    @Transactional
    public Map<String, Object> getGrnById(Integer grnId) {
        Inventory inv = inventoryRepository.findById(grnId)
                .orElseThrow(() -> new IllegalArgumentException("GRN not found: " + grnId));

        StockLots stockLot = (inv.getStockLots() != null && !inv.getStockLots().isEmpty())
                ? inv.getStockLots().get(0)
                : null;

        Map<String, Object> result = new HashMap<>();
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
        result.put("materialName",     inv.getVariant() != null ? (inv.getVariant().getMaterial().getMaterial() + " (GSM: " + inv.getVariant().getGsm() + ")") : "-");
        result.put("receivedByUsername", inv.getReceivedByUser() != null ? inv.getReceivedByUser().getUsername() : "-");
        return result;
    }

    //  Update GRN and its linked StockLot  
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

    //  Delete StockLot and its linked Inventory record
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

    //  Save Returned Stock as a Stock Lot
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

        productionRepo.findByOrderId(dto.getJobNo().trim())
                .orElseThrow(() -> new IllegalArgumentException("Production job not found."));

        List<ProductionStockReservation> reservations = reservationRepo
                .findByProductionOrderIdAndStockLotVariantIdOrderById(dto.getJobNo().trim(), dto.getVariantId());
        if (reservations.isEmpty()) {
            throw new IllegalArgumentException("This material was not issued to the selected production job.");
        }

        int returnableQuantity = reservations.stream()
                .mapToInt(reservation -> Math.max(0,
                        safeQuantity(reservation.getUsedQuantity()) - safeQuantity(reservation.getReturnedQuantity())))
                .sum();
        if (dto.getReturnedQty() > returnableQuantity) {
            throw new IllegalArgumentException("Returned quantity exceeds the material issued and not already returned for this job.");
        }

        int remainingToAllocate = dto.getReturnedQty();
        for (ProductionStockReservation reservation : reservations) {
            int returnableFromReservation = Math.max(0,
                    safeQuantity(reservation.getUsedQuantity()) - safeQuantity(reservation.getReturnedQuantity()));
            int returnedFromReservation = Math.min(remainingToAllocate, returnableFromReservation);
            if (returnedFromReservation == 0) continue;

            reservation.setReturnedQuantity(safeQuantity(reservation.getReturnedQuantity()) + returnedFromReservation);
            reservationRepo.save(reservation);
            remainingToAllocate -= returnedFromReservation;
            if (remainingToAllocate == 0) break;
        }

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

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    /** Consumes only stock already reserved for the production job. */
    @Transactional
    public void recordUsage(MaterialUsageDto dto) {
        if (dto == null || dto.getVariantId() == null || dto.getQuantityUsed() == null || dto.getQuantityUsed() <= 0 || dto.getJobNo() == null || dto.getJobNo().isBlank())
            throw new IllegalArgumentException("Job, material variant, and a positive quantity are required.");
        int remaining = dto.getQuantityUsed();
        List<ProductionStockReservation> reservations = reservationRepo
                .findByProductionOrderIdAndStockLotVariantIdOrderById(dto.getJobNo().trim(), dto.getVariantId());
        for (ProductionStockReservation reservation : reservations) {
            int unused = reservation.getReservedQuantity() - reservation.getUsedQuantity();
            int use = Math.min(unused, remaining);
            if (use <= 0) continue;
            StockLots lot = reservation.getStockLot();
            lot.setQuantity(lot.getQuantity() - use);
            lot.setReservedQuantity(Math.max(0, lot.getReservedQuantity() - use));
            if (lot.getQuantity() == 0) lot.setStatus("Consumed"); else if (lot.getReservedQuantity() == 0) lot.setStatus("Available");
            reservation.setUsedQuantity(reservation.getUsedQuantity() + use);
            stockLotsRepo.save(lot); reservationRepo.save(reservation);
            remaining -= use;
            if (remaining == 0) return;
        }
        throw new IllegalArgumentException("Usage exceeds stock reserved for this job.");
    }

    @Transactional
    public Materials addMaterials(){
        Materials materials = new Materials();
        materials.setMaterial("New Material");
        materials.setStatus("Active");
        return materialRepo.save(materials);
    }

    

}
