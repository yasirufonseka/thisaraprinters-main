package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.PriceRequestDto;
import com.example.thisaraprinters.dto.PriceRequestReplyDto;
import com.example.thisaraprinters.dto.SupplierDto;
import com.example.thisaraprinters.model.PriceRequestReply;
import com.example.thisaraprinters.model.PurchaseOrder;
import com.example.thisaraprinters.model.Supplier;
import com.example.thisaraprinters.repository.SupplierRepo;
import com.example.thisaraprinters.service.SupplierService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/supplier")
@Controller
public class SupplierController {

    @Autowired
    private final SupplierService supplierService;
    private SupplierRepo supplierRepo;

    SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/suppliermodel")
    public ModelAndView showUserModel() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("supplier");
        mav.addObject("suppliers", supplierService.getAllUsers());
        mav.addObject("priceRequests", supplierService.getAllPriceRequests());
        mav.addObject("materialsList", supplierService.getAllMaterials());
        mav.addObject("categoryList",supplierService.getAllCategory());
        return mav;
    }

    @GetMapping("/pricerequest/form")
    public ModelAndView showForm(@RequestParam(value = "priceRequestId", required = false) Integer priceRequestId,
                                 @RequestParam(value = "supplierId", required = false) Integer supplierId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("priceRequestForm");
        mav.addObject("priceRequestId", priceRequestId);
        mav.addObject("supplierId", supplierId);
        return mav;
    }


    @PostMapping("/add/supplier")
    public ResponseEntity<?> addSupplier(@RequestBody SupplierDto supplierData) {
        try{
        return ResponseEntity.status(200).body(Map.of("message" , supplierService.addSupllier(supplierData)));
        }catch(Exception e){
            throw  new RuntimeException(e);
        }
    }
    @GetMapping("/getsupplier/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable("id") int id) {
        return ResponseEntity.status(200).body(supplierService.getSupplierById(id));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateSupplier(@RequestBody SupplierDto supplierData, @PathVariable("id") int id) {
        return ResponseEntity.status(200).body(Map.of("message", supplierService.updateSupplier(supplierData, id)));
    }

    //price requset part
    @PostMapping("/pricerequest")
    public ResponseEntity<Map<String, String>> getPricerequest(@RequestBody PriceRequestDto request) {
        try {
            if (request.getSupplierlist() == null) {
                return ResponseEntity.status(400).body(Map.of("message", "Needs to select one or more suppliers"));
            }
            return ResponseEntity.status(200).body(Map.of("message", supplierService.getPricerequest(request)));
        }catch(Exception e){
            throw  new RuntimeException(e);


        }
    }

    // Save supplier's reply to a price request (from the form page)
    @PostMapping("/pricerequest/reply")
    public ResponseEntity<Map<String, String>> savePriceRequestReply(@RequestBody PriceRequestReplyDto replyDto) {
        try {
            return ResponseEntity.status(200).body(Map.of("message", supplierService.savePriceRequestReply(replyDto)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to save reply: " + e.getMessage()));
        }
    }

    // Get all replies for a specific price request (AJAX call from supplier page)
    @GetMapping("/pricerequest/replies/{id}")
    public ResponseEntity<List<PriceRequestReply>> getReplies(@PathVariable("id") int id) {
        return ResponseEntity.status(200).body(supplierService.getRepliesByPriceRequestId(id));
    }

    // Get a specific price request by ID
    @GetMapping("/pricerequest/{id}")
    public ResponseEntity<com.example.thisaraprinters.model.PriceRequest> getPriceRequestById(@PathVariable("id") int id) {
        return ResponseEntity.status(200).body(supplierService.getPriceRequestById(id));
    }
    
    // Get completed price requests
    @GetMapping("/pricerequests/completed")
    public ResponseEntity<List<com.example.thisaraprinters.model.PriceRequest>> getCompletedPriceRequests() {
        return ResponseEntity.status(200).body(supplierService.getCompletedPriceRequests());
    }

    // Purchase Order Endpoints
    @PostMapping("/purchaseorder")
    public ResponseEntity<Map<String, String>> addPurchaseOrder(@RequestBody com.example.thisaraprinters.dto.PurchaseOrderDto dto) {
        try {
            return ResponseEntity.status(200).body(Map.of("message", supplierService.addPurchaseOrder(dto)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/purchaseorder/update/{id}")
    public ResponseEntity<Map<String, String>> updatePurchaseOrder(@RequestBody com.example.thisaraprinters.dto.PurchaseOrderDto dto, @PathVariable("id") Integer id) {
        try {
            return ResponseEntity.status(200).body(Map.of("message", supplierService.updatePurchaseOrder(id, dto)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/purchaseorders")
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.status(200).body(supplierService.getAllPurchaseOrders());
    }

    @DeleteMapping("/purchaseorder/delete/{id}")
    public ResponseEntity<Map<String, String>> deletePurchaseOrder(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.status(200).body(Map.of("message", supplierService.deletePurchaseOrder(id)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/purchaseorder/{id}/payment")
    public ResponseEntity<Map<String, String>> updatePaymentStatus(
            @PathVariable("id") Integer orderId,
            @RequestParam("paymentStatus") String paymentStatus,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "paidAmount", required = false) Double paidAmount,
            @RequestParam(value = "paymentProof", required = false) MultipartFile paymentProof,
            @RequestParam(value = "paymentNotes", required = false) String paymentNotes) {
        try {
            String paymentProofFileName = null;
            
            if (paymentProof != null && !paymentProof.isEmpty()) {
                // Validate file
                if (paymentProof.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(400).body(Map.of("message", "File size exceeds 5MB limit"));
                }
                
                String contentType = paymentProof.getContentType();
                if (!isValidFileType(contentType)) {
                    return ResponseEntity.status(400).body(Map.of("message", "Invalid file type. Only PDF, JPG, PNG are allowed"));
                }
                
                // Save file to uploads directory
                String uploadDir = "src/main/resources/static/uploads/payment-proofs/";
                new File(uploadDir).mkdirs();
                
                String fileName = UUID.randomUUID() + "_" + paymentProof.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                
                Files.write(filePath, paymentProof.getBytes());
                paymentProofFileName = fileName;
            }
            
            return ResponseEntity.status(200).body(Map.of("message", 
                    supplierService.updatePaymentStatus(orderId, paymentStatus, paymentMethod, paidAmount, paymentProofFileName, paymentNotes)));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to save payment proof: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("application/pdf") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.startsWith("image/") ||
               contentType.equals("application/octet-stream");
    }

    @GetMapping("get/materials")
    public ResponseEntity<?> getMaterials(){
        return ResponseEntity.status(200).body(supplierService.getAllMaterials());

    }
}
