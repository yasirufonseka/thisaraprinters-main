package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ── Page ──────────────────────────────────────────────
    @GetMapping
    public ModelAndView reportsPage() {
        ModelAndView mav = new ModelAndView("reports");
        return mav;
    }

    // ── 1. Sales / Quotation Summary Report ──────────────
    @GetMapping("/sales")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salesReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getSalesReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Sales report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // Stock-Lot Report
    @GetMapping("/inventory")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventoryReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getInventoryReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Inventory report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // ── 3. GRN Report ────────────────────────────────────
    @GetMapping("/grn")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> grnReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getGrnReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "GRN report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // ── 4. Purchase Order Report ──────────────────────────
    @GetMapping("/purchase-orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> purchaseOrderReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getPurchaseOrderReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Purchase order report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // ── 5. Production Status Report ───────────────────────
    @GetMapping("/production")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> productionReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getProductionReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Production report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // ── 6. Supplier Price Comparison ──────────────────────
    @GetMapping("/supplier-price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> supplierPriceReport(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            Map<String, Object> result = reportService.getSupplierPriceReport(start, end);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Supplier price report failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
