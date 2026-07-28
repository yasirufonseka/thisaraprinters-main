package com.example.thisaraprinters.service;

import com.example.thisaraprinters.model.ProductionModel;
import com.example.thisaraprinters.repository.ProductionRepo;
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

@Service
public class ProductionService {

    private final ProductionRepo productionRepo;

    public ProductionService(ProductionRepo productionRepo) {
        this.productionRepo = productionRepo;
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

    public void updateJobStatus(String orderId, String newStatus) {
        ProductionModel job = getJobByOrderId(orderId);
        job.setStatus(newStatus);
        productionRepo.save(job);
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

    public ProductionModel createJob(ProductionModel job) {
        return productionRepo.save(job);
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
