package com.example.thisaraprinters.service;

import com.example.thisaraprinters.repository.EmployeeRepo;
import java.time.LocalDate;
import java.util.List;

import com.example.thisaraprinters.repository.ModuleRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.thisaraprinters.dto.UserDto;
import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.model.RoleModel;
import com.example.thisaraprinters.model.UserModel;
import com.example.thisaraprinters.repository.RoleRepo;
import com.example.thisaraprinters.repository.UserRepo;

@Service
public class UserService {

    private final EmployeeRepo employeeRepo;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, RoleRepo roleRepo, EmployeeRepo employeeRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserModel> getAllUsers() {

        return userRepo.findAll();
    }

    public String saveUser(UserDto user) {
        UserModel newUser = new UserModel();
        if (user.getEmployeeid() != null) {
            newUser.setEmployeeid(employeeRepo.findById(user.getEmployeeid()).orElse(null));
        }
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setNote(user.getNote());
        
        if (user.getRoleId() != null) {
            RoleModel role = roleRepo.findById(user.getRoleId()).orElse(null);
            newUser.setRole(role);
        }

        newUser.setStatus(user.getStatus());
        newUser.setAddeddate(LocalDate.now());
        newUser.setUserphoto(user.getUserphoto());
        userRepo.save(newUser);
        return "User saved successfully";
    }

    // get all employees
    public List<EmployeeModel> getEmployeeList() {
        return employeeRepo.findAll();
    }

    public String updateUser(UserDto user, Integer id) {
        if (id == null) {
            return "User ID is required for update";
        }

        UserModel existingUser = userRepo.findById(id).orElse(null);
        UserModel userWithSameUsername = userRepo.findByUsername(user.getUsername());

        if (existingUser == null) {
            return "User not found";
        }
        if(userWithSameUsername != null && userWithSameUsername.getId() != existingUser.getId()) {
            System.out.println(existingUser.getId());
            return "Username already exists please choose another username";

        }
        else{       
            existingUser.setEmployeeid(employeeRepo.findById(user.getEmployeeid()).orElse(null));
            existingUser.setUsername(user.getUsername());
            // Only update password if a new one was actually provided
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            existingUser.setNote(user.getNote());
            existingUser.setStatus(user.getStatus());
            existingUser.setUserphoto(user.getUserphoto());
            if (user.getRoleId() != null) {
                RoleModel role = roleRepo.findById(user.getRoleId()).orElse(null);
                existingUser.setRole(role);
            } else {
                existingUser.setRole(null);
            }
        }

        existingUser.setUpdateddate(LocalDate.now());
        
        userRepo.save(existingUser);
        return "User updated successfully";
    }

    public UserModel getUserById(int id) {
        return userRepo.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
    };

    public List<RoleModel> getRoles(){
        return roleRepo.findAll();
    }

    public String deleteUser(Integer id) {
        UserModel user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepo.delete(user);
        return "User deleted successfully";
    }

}
