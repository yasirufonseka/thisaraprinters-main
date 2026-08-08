package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.CustomerPaymentDto;
import com.example.thisaraprinters.model.CustomerPayment;
import com.example.thisaraprinters.model.PurchaseOrder;
import com.example.thisaraprinters.service.PaymentService;
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

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ─── Page Route ────────────────────────────────────────────────────────────

    @GetMapping("/management")
    public ModelAndView paymentPage() {
        ModelAndView mav = new ModelAndView("payment");
        // Pass quotations list so the customer payments tab can show all records
        mav.addObject("quotations", paymentService.getAllQuotations());
        mav.addObject("productions",paymentService.getAllProductions());
        mav.addObject("payments", paymentService.getAllSupplierPayments());
        return mav;
    }

    // Supplier Payment Endpoints

    @GetMapping("/supplier-orders")
    @ResponseBody
    public List<PurchaseOrder> getAllSupplierOrders() {
        return paymentService.getAllSupplierOrders();
    }

    @PostMapping("/supplier/{id}/payment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateSupplierPayment(
            @PathVariable("id") Integer orderId,
            @RequestParam("paymentStatus") String paymentStatus,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "paidAmount", required = false) Double paidAmount,
            @RequestParam(value = "paymentProof", required = false) MultipartFile paymentProof,
            @RequestParam(value = "paymentNotes", required = false) String paymentNotes) {
        try {
            String proofFileName = null;

            if (paymentProof != null && !paymentProof.isEmpty()) {
                if (paymentProof.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(400).body(Map.of("message", "File size exceeds 5MB limit"));
                }
                String contentType = paymentProof.getContentType();
                if (!isValidFileType(contentType)) {
                    return ResponseEntity.status(400).body(Map.of("message", "Invalid file type. Only PDF, JPG, PNG allowed"));
                }
                String uploadDir = "src/main/resources/static/uploads/payment-proofs/";
                new File(uploadDir).mkdirs();
                String fileName = UUID.randomUUID() + "_" + paymentProof.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, paymentProof.getBytes());
                proofFileName = fileName;
            }

            String result = paymentService.updateSupplierPayment(
                    orderId, paymentStatus, paymentMethod, paidAmount, proofFileName, paymentNotes);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Customer Payment Endpoints ────────────────────────────────────────────

    @GetMapping("/customer-payments")
    @ResponseBody
    public List<CustomerPayment> getAllCustomerPayments() {
        return paymentService.getAllCustomerPayments();
    }

    @PostMapping("/customer/add")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addCustomerPayment(@RequestBody CustomerPaymentDto dto) {
        try {
            String result = paymentService.addCustomerPayment(dto);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/customer/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateCustomerPayment(
            @PathVariable("id") Integer id,
            @RequestBody CustomerPaymentDto dto) {
        try {
            String result = paymentService.updateCustomerPayment(id, dto);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/invoice/{id}")
    @ResponseBody
    public ResponseEntity<CustomerPayment> getInvoiceData(@PathVariable("id") Integer paymentId) {
        try {
            CustomerPayment data = paymentService.getInvoiceData(paymentId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("application/pdf") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.startsWith("image/") ||
               contentType.equals("application/octet-stream");
    }
}
