package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.dto.EmployeeDto;
import com.example.thisaraprinters.model.DesignationModel;
import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    // automaticaly create a instence of employee service
    @Autowired
    private EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;

    }

    @GetMapping("/getemployees")
    public ModelAndView showEmployees() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("employee");
        return mav;
    }

    @PostMapping("/add/employee")
    public ResponseEntity<?> addEmployee(@ModelAttribute EmployeeDto employee, @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            System.out.println(employee.getFullname());
            employeeService.saveEmployee(employee, image);
            return ResponseEntity.ok(Map.of("message", "successfully added the employee"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error adding employee: " + e.getMessage()));
        }
    }

    @GetMapping("/get/alldata")
    public ResponseEntity<?> getAllEmployees() {
        
            List<EmployeeModel> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
       
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable("id") Long id, @ModelAttribute EmployeeDto employee, @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            employeeService.updateEmployee(id, employee, image);
            return ResponseEntity.ok(Map.of("message", "successfully updated the employee"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error updating employee"));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable("id") Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(Map.of("message", "successfully deleted the employee"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error deleting employee"));
        }
    }

    @GetMapping("/get/designations")
    public ResponseEntity<List<DesignationModel>> getDesignations() {
        return ResponseEntity.ok(employeeService.getAllDesignations());
    }

}
