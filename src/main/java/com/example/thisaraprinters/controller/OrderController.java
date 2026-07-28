package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.QuotationDto;
import com.example.thisaraprinters.model.CustomerModel;
import com.example.thisaraprinters.model.MaterialVariant;
import com.example.thisaraprinters.model.QuotationModel;
import com.example.thisaraprinters.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    public  OrderController(OrderService orderService){
        this.orderService = orderService;
    }
    @GetMapping("/management")
    public ModelAndView getOrderView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("ordermanagement");
        mav.addObject("materialList",orderService.getAllMaterialVariant());
        mav.addObject("customerList", toCustomerSearchList(orderService.getAllCustomers()));
      //  mav.addObject("materialList",orderService.getAllMaterials());
        return mav;
    }

    private List<Map<String, Object>> toCustomerSearchList(List<CustomerModel> customers) {
        return customers.stream()
                .map(customer -> {
                    Map<String, Object> customerSearchData = new HashMap<>();
                    customerSearchData.put("id", customer.getId());
                    customerSearchData.put("name", customer.getName());
                    customerSearchData.put("email", customer.getEmail());
                    customerSearchData.put("phone", customer.getPhone());
                    customerSearchData.put("address", customer.getAddress());
                    return customerSearchData;
                })
                .toList();
    }

    @PostMapping("/save/quotation")
    public ResponseEntity<?> saveQuotation(@RequestBody QuotationDto quotation) {
       return ResponseEntity.status(200).body(Map.of("message", orderService.saveQuotation(quotation)));
    }

    @GetMapping("/material-price/{variantId}")
    public ResponseEntity<Map<String, Object>> getMaterialPrice(@PathVariable("variantId") Integer variantId) {
        Double sheetRate = orderService.getLatestSheetRate(variantId);
        MaterialVariant variant = orderService.getMaterialVariantById(variantId);
        if (variant != null) {
            return ResponseEntity.ok(Map.of(
                "variantId", variantId,
                "sheetRate", sheetRate,
                "width", variant.getWidth() != null ? variant.getWidth() : 0.0,
                "height", variant.getHeight() != null ? variant.getHeight() : 0.0,
                "gsm", variant.getGsm() != null ? variant.getGsm() : 0
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/quotations")
    @ResponseBody
    public List<QuotationModel> getAllQuotations() {
        return orderService.getAllQuotations();
    }

    @DeleteMapping("/quotation/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteQuotation(@PathVariable("id") int id) {
        try {
            orderService.deleteQuotation(id);
            return ResponseEntity.ok(Map.of("message", "Quotation deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/send-to-production/{id}")
    @ResponseBody
    public ResponseEntity<?> sendToProduction(@PathVariable("id") int id, @RequestBody Map<String, Object> payload) {
        try {
            String priority = (String) payload.get("priority");
            String deadlineStr = (String) payload.get("deadline");
            LocalDate deadline = (deadlineStr != null && !deadlineStr.isEmpty()) ? LocalDate.parse(deadlineStr) : null;
            double advanceAmount = 0.0;
            Object advObj = payload.get("advanceAmount");
            if (advObj instanceof Number) {
                advanceAmount = ((Number) advObj).doubleValue();
            }
            String msg = orderService.sendToProduction(id, priority, deadline, advanceAmount);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}
