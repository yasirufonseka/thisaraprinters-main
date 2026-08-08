package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.EmployeeDto;
import com.example.thisaraprinters.model.DesignationModel;
import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.model.UserModel;
import com.example.thisaraprinters.repository.DesignationRepo;
import com.example.thisaraprinters.repository.EmployeeRepo;
import com.example.thisaraprinters.repository.UserRepo;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {
    // create an instence of the repository

    private final EmployeeRepo employeeRepo;
    private final UserRepo userRepo;
    private final DesignationRepo designationRepo;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepo employeeRepo, UserRepo userRepo, DesignationRepo designationRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.userRepo = userRepo;
        this.designationRepo = designationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String saveEmployee(EmployeeDto employee, MultipartFile image) {
        try {

            if (employee == null) {
                return "Employee is null";
            } else {
                System.out.println("Employee data: " + employee);

                EmployeeModel newEmployee = new EmployeeModel();
                newEmployee.setFullname(employee.getFullname());
                newEmployee.setCallingname(employee.getCallingname());
                newEmployee.setNic(employee.getNic());
                newEmployee.setDob(employee.getDob());
                newEmployee.setGender(employee.getGender());
                newEmployee.setEmail(employee.getEmail());
                newEmployee.setPhonenumber(employee.getPhonenumber());
                newEmployee.setAddress(employee.getAddress());
                newEmployee.setEmgpersonname(employee.getEmgpersonname());
                newEmployee.setEmgpersonphonenumber(employee.getEmgpersonphonenumber());
                newEmployee.setAddeddate(LocalDate.now());
                newEmployee.setDesignationid(designationRepo.findById(employee.getDesignationid()).get());
                newEmployee.setUpdateddate(null);
                if (image != null && !image.isEmpty()) {
                    newEmployee.setImage(image.getBytes());
                }
                EmployeeModel savedEmployee = employeeRepo.save(newEmployee);

                // save user info for user table to create system user profile for the employee
                UserModel newUser = new UserModel();
                newUser.setUsername(employee.getEmail());
                newUser.setPassword(passwordEncoder.encode(employee.getNic())); // Set initial password as NIC (hashed)
                newUser.setAddeddate(LocalDate.now());
                newUser.setUpdateddate(null);
                newUser.setNote("User created by system");
                newUser.setStatus("Active");
                newUser.setUserphoto("");
                // save the employee
                newUser.setEmployeeid(savedEmployee);
                // save the user
                userRepo.save(newUser);

                return "Adding employee is success!";
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving employee: " + e.getMessage());
        }
    }

    public List<EmployeeModel> getAllEmployees() {

        return employeeRepo.findAll();
    }

    public String updateEmployee(Long id, EmployeeDto employee, MultipartFile image) {
        try {
            System.out.println(id);

            EmployeeModel existingEmployee = employeeRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

            existingEmployee.setFullname(employee.getFullname());
            existingEmployee.setCallingname(employee.getCallingname());
            existingEmployee.setNic(employee.getNic());
            existingEmployee.setDob(employee.getDob());
            existingEmployee.setGender(employee.getGender());
            existingEmployee.setEmail(employee.getEmail());
            existingEmployee.setPhonenumber(employee.getPhonenumber());
            existingEmployee.setAddress(employee.getAddress());
            existingEmployee.setEmgpersonname(employee.getEmgpersonname());
            existingEmployee.setEmgpersonphonenumber(employee.getEmgpersonphonenumber());
            existingEmployee.setDesignationid(designationRepo.findById(employee.getDesignationid()).get());
            if (image != null && !image.isEmpty()) {
                existingEmployee.setImage(image.getBytes());
            }
            existingEmployee.setUpdateddate(LocalDate.now());
            // set updated designation

            employeeRepo.save(existingEmployee);
            return "Employee updated";

        } catch (Exception e) {
            String message = e.getMessage();
            return message;
        }
    }

    // delete employee
    public String deleteEmployee(Long id) {
        EmployeeModel existingEmployee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employeeRepo.delete(existingEmployee);
        return "Employee deleted";
    }

    // get all designations
    public List<DesignationModel> getAllDesignations() {
        System.out.println(designationRepo.findAll());
        return designationRepo.findAll();
    }
}
