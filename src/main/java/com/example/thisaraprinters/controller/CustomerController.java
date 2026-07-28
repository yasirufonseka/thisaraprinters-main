package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.CustomerDto;
import com.example.thisaraprinters.model.CustomerModel;
import com.example.thisaraprinters.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customermodel")
    public ModelAndView customerPage() {
        ModelAndView mav = new ModelAndView("customer");
        mav.addObject("customers", customerService.getAllCustomers());
        return mav;
    }

    @GetMapping("/all")
    @ResponseBody
    public List<CustomerModel> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping("/add/customer")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addCustomer(@jakarta.validation.Valid @RequestBody CustomerDto customerDto) {
        customerService.addCustomer(customerDto);
        return ResponseEntity.ok(Map.of("message", "Customer added successfully"));
    }

    @GetMapping("/getcustomer/{id}")
    @ResponseBody
    public CustomerModel getCustomer(@PathVariable("id") Integer id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateCustomer(@jakarta.validation.Valid @RequestBody CustomerDto customerDto, @PathVariable("id") Integer id) {
        customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(Map.of("message", "Customer updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable("id") Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));
    }
}

