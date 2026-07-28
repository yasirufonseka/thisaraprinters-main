package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.model.RoleModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.example.thisaraprinters.dto.UserDto;
import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.model.UserModel;
import com.example.thisaraprinters.model.Module;
import com.example.thisaraprinters.service.UserService;

import java.util.List;
import java.util.Map;

import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/user")
public class UserController {

    // show model
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/usermodel")
    public ModelAndView showUserModel() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user");
        mav.addObject("users", userService.getAllUsers());
        return mav;
    }

    // get all users
    @GetMapping("/getallusers")
    public ResponseEntity<List<UserModel>> getAllUsers() {
        List<UserModel> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/add/user")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody UserDto user) {
        return ResponseEntity.status(200).body(Map.of("message", userService.saveUser(user)));
    }

    @PutMapping("/update/user/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@RequestBody UserDto user, @PathVariable("id") Integer id) {
        System.out.println("request recived");
        return ResponseEntity.status(200).body(Map.of("message", userService.updateUser(user, id)));
    }

    @GetMapping("/getemployeelist")
    public ResponseEntity<List<EmployeeModel>> getEmployeeList() {
        return ResponseEntity.status(200).body(userService.getEmployeeList());
    }

    @GetMapping("/getuserbyid/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable("id") int id) {
        return ResponseEntity.status(200).body(userService.getUserById(id));
    }

    @GetMapping("/getuser/roles")
    @ResponseBody
    public ResponseEntity<List<RoleModel>> getRoles() {
        return ResponseEntity.status(200).body(userService.getRoles());
    }

    @GetMapping("/getmodule")
    public ResponseEntity<List<Module>> getModules() {
        return ResponseEntity.status(200).body(userService.getAllModules());
    }
}
