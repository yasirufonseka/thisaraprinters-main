package com.example.thisaraprinters.service;

import com.example.thisaraprinters.model.ProductionModel;
import com.example.thisaraprinters.model.ProductionStatusHistory;
import com.example.thisaraprinters.model.ProductionStockReservation;
import com.example.thisaraprinters.model.StockLots;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.repository.ProductionStatusHistoryRepo;
import com.example.thisaraprinters.repository.ProductionStockReservationRepo;
import com.example.thisaraprinters.repository.ProductionRepo;
import com.example.thisaraprinters.repository.StockLotsRepo;
import com.example.thisaraprinters.repository.EmployeeRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionService {

    private final ProductionRepo productionRepo;
    private final ProductionStatusHistoryRepo statusHistoryRepo;
    private final ProductionStockReservationRepo reservationRepo;
    private final StockLotsRepo stockLotsRepo;
    private final EmployeeRepo employeeRepo;

    public ProductionService(ProductionRepo productionRepo, ProductionStatusHistoryRepo statusHistoryRepo,
                             ProductionStockReservationRepo reservationRepo, StockLotsRepo stockLotsRepo, EmployeeRepo employeeRepo) {
        this.productionRepo = productionRepo;
        this.statusHistoryRepo = statusHistoryRepo;
        this.reservationRepo = reservationRepo;
        this.stockLotsRepo = stockLotsRepo;
        this.employeeRepo = employeeRepo;
    }

    @PostConstruct
    public void seedMockData() {
        // Delete previously seeded mock data
        List<String> mockOrderIds = List.of("ORD-1025", "ORD-1015", "ORD-1030", "ORD-1010", "ORD-1005");
        for (String orderId : mockOrderIds) {
            productionRepo.findByOrderId(orderId).ifPresent(productionRepo::delete);
        }
    }

    public List<ProductionModel> getAllJobs() {
        return productionRepo.findAll();
    }

    public ProductionModel getJobById(int id) {
        return productionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Production job not found for ID: " + id));
    }

    public ProductionModel getJobByOrderId(String orderId) {
        return productionRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Production job not found for Order ID: " + orderId));
    }

    @Transactional
    public void updateJobStatus(String orderId, String newStatus, String changedBy) {
        Set<String> allowed = Set.of("New Orders", "Design Phase", "Printing", "Finishing", "Ready to Deliver", "Dispatched");
        if (!allowed.contains(newStatus)) throw new IllegalArgumentException("Unsupported production status");
        ProductionModel job = getJobByOrderId(orderId);
        job.setStatus(newStatus);
        LocalDate today = LocalDate.now();
        if ("Printing".equals(newStatus) && job.getStartedAt() == null) job.setStartedAt(today);
        if ("Ready to Deliver".equals(newStatus) && job.getCompletedAt() == null) job.setCompletedAt(today);
        if ("Dispatched".equals(newStatus) && job.getDeliveredAt() == null) job.setDeliveredAt(today);
        productionRepo.save(job);
        addHistory(job, newStatus, changedBy);
    }

    @Transactional
    public void assignEmployee(String orderId, Long employeeId, String changedBy) {
        ProductionModel job = getJobByOrderId(orderId);
        job.setAssignedEmployee(employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found.")));
        productionRepo.save(job);
        addHistory(job, "Assigned", changedBy);
    }

    public void deleteJob(int id) {
        if (!productionRepo.existsById(id)) {
            throw new RuntimeException("Production job not found for ID: " + id);
        }
        productionRepo.deleteById(id);
    }

    public void deleteJobByOrderId(String orderId) {
        ProductionModel job = getJobByOrderId(orderId);
        productionRepo.delete(job);
    }

    @Transactional
    public ProductionModel createJob(ProductionModel job) {
        if (job.getDateSentToProduction() == null) job.setDateSentToProduction(LocalDate.now());
        if (job.getStatus() == null) job.setStatus("New Orders");
        ProductionModel saved = productionRepo.save(job);
        reserveRequiredMaterial(saved);
        addHistory(saved, saved.getStatus(), "system");
        return saved;
    }

    private void reserveRequiredMaterial(ProductionModel job) {
        if (job.getQuotationid() == null || job.getQuotationid().getMaterialsList() == null ||
                job.getQuotationid().getMaterialsList().isEmpty() || job.getTotalSheetsNeeded() == null || job.getTotalSheetsNeeded() <= 0) return;
        MaterialVariant variant = job.getQuotationid().getMaterialsList().get(0);
        int remaining = job.getTotalSheetsNeeded();
        for (StockLots lot : stockLotsRepo.findByVariantIdOrderByCreatedAtAsc(variant.getId())) {
            if (!"Available".equalsIgnoreCase(lot.getStatus()) && !"Reserved".equalsIgnoreCase(lot.getStatus())) continue;
            int available = Math.max(0, (lot.getQuantity() == null ? 0 : lot.getQuantity()) - (lot.getReservedQuantity() == null ? 0 : lot.getReservedQuantity()));
            int reserve = Math.min(available, remaining);
            if (reserve == 0) continue;
            lot.setReservedQuantity((lot.getReservedQuantity() == null ? 0 : lot.getReservedQuantity()) + reserve);
            if (available == reserve) lot.setStatus("Reserved");
            stockLotsRepo.save(lot);
            ProductionStockReservation reservation = new ProductionStockReservation();
            reservation.setProduction(job); reservation.setStockLot(lot); reservation.setReservedQuantity(reserve); reservation.setUsedQuantity(0);
            reservationRepo.save(reservation);
            remaining -= reserve;
            if (remaining == 0) return;
        }
        throw new IllegalStateException("Insufficient available stock for production job " + job.getOrderId());
    }

    private void addHistory(ProductionModel job, String status, String changedBy) {
        ProductionStatusHistory event = new ProductionStatusHistory();
        event.setProduction(job); event.setStatus(status); event.setChangedAt(java.time.LocalDateTime.now()); event.setChangedBy(changedBy);
        statusHistoryRepo.save(event);
    }

    // Upload artwork for a production job
    public ProductionModel uploadArtwork(String orderId, MultipartFile file) throws IOException {
        ProductionModel job = getJobByOrderId(orderId);

        // Save to external uploads directory (served via WebMvcConfig resource handler)
        String uploadDir = "uploads/artwork/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Delete old file if present
        if (job.getArtworkPath() != null) {
            Path oldFile = Paths.get(job.getArtworkPath());
            if (Files.exists(oldFile)) {
                Files.delete(oldFile);
            }
        }

        String ext = "";
        String origName = file.getOriginalFilename();
        if (origName != null && origName.contains(".")) {
            ext = origName.substring(origName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID().toString() + ext;
        Path savePath = uploadPath.resolve(savedName);
        Files.write(savePath, file.getBytes());

        job.setArtworkPath(uploadDir + savedName); // store full relative path for file deletion
        job.setArtworkOriginalName(origName);
        return productionRepo.save(job);
    }

    // Delete artwork for a production job
    public void deleteArtwork(String orderId) throws IOException {
        ProductionModel job = getJobByOrderId(orderId);
        if (job.getArtworkPath() != null) {
            Path filePath = Paths.get(job.getArtworkPath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            job.setArtworkPath(null);
            job.setArtworkOriginalName(null);
            productionRepo.save(job);
        }
    }
}
