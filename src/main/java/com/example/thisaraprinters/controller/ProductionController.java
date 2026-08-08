package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.model.ProductionModel;
import com.example.thisaraprinters.service.ProductionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping("/management")
    public ModelAndView getProductionView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("production");
        return mav;
    }

    @GetMapping("/staff")
    public ModelAndView getStaffView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("staff");
        return mav;
    }

    @GetMapping("/all")
    @ResponseBody
    public List<ProductionModel> getAllProductionJobs() {
        return productionService.getAllJobs();
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public ProductionModel getProductionJob(@PathVariable("id") int id) {
        return productionService.getJobById(id);
    }

    @GetMapping("/get-by-order/{orderId}")
    @ResponseBody
    public ProductionModel getProductionJobByOrderId(@PathVariable("orderId") String orderId) {
        return productionService.getJobByOrderId(orderId);
    }

    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStatus(@RequestBody Map<String, String> payload, Authentication authentication) {
        String orderId = payload.get("orderId");
        String status = payload.get("status");
        if (orderId == null || status == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "orderId and status are required"));
        }
        productionService.updateJobStatus(orderId, status, authentication != null ? authentication.getName() : "system");
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    @PostMapping("/assign")
    @ResponseBody
    public ResponseEntity<Map<String, String>> assignEmployee(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Object employeeId = payload.get("employeeId");
        String orderId = (String) payload.get("orderId");
        if (orderId == null || !(employeeId instanceof Number id)) return ResponseEntity.badRequest().body(Map.of("message", "orderId and employeeId are required"));
        productionService.assignEmployee(orderId, id.longValue(), authentication != null ? authentication.getName() : "system");
        return ResponseEntity.ok(Map.of("message", "Employee assigned successfully"));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable("id") int id) {
        productionService.deleteJob(id);
        return ResponseEntity.ok(Map.of("message", "Production job deleted successfully"));
    }

    @DeleteMapping("/delete-by-order/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteJobByOrderId(@PathVariable("orderId") String orderId) {
        productionService.deleteJobByOrderId(orderId);
        return ResponseEntity.ok(Map.of("message", "Production job deleted successfully"));
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<ProductionModel> saveJob(@RequestBody ProductionModel job) {
        return ResponseEntity.ok(productionService.createJob(job));
    }

    @PostMapping("/upload-artwork/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadArtwork(
            @PathVariable("orderId") String orderId,
            @RequestParam("file") MultipartFile file) {
        try {
            ProductionModel updated = productionService.uploadArtwork(orderId, file);
            return ResponseEntity.ok(Map.of(
                "message", "Artwork uploaded successfully",
                "artworkPath", updated.getArtworkPath(),
                "artworkOriginalName", updated.getArtworkOriginalName() != null ? updated.getArtworkOriginalName() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-artwork/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteArtwork(@PathVariable("orderId") String orderId) {
        try {
            productionService.deleteArtwork(orderId);
            return ResponseEntity.ok(Map.of("message", "Artwork deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Delete failed: " + e.getMessage()));
        }
    }
}
