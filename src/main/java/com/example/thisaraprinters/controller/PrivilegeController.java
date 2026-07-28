package com.example.thisaraprinters.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thisaraprinters.dto.PrivilegeDto;
import com.example.thisaraprinters.model.PrivilegeModel;
import com.example.thisaraprinters.service.PrivilegeService;

@RestController
@RequestMapping("/privilege")
public class PrivilegeController {

    private final PrivilegeService privilegeService;
    //import privilage service class into controller class
    public PrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    /**
     * Get all available module names in the system
     */
    @GetMapping("/modules")
    public ResponseEntity<List<String>> getAllModules() {
        return ResponseEntity.ok(privilegeService.getAllModules());
    }

    /**
     * Get all privileges assigned to a specific role
     */
    @GetMapping("/getbyrole/{roleId}")
    public ResponseEntity<List<PrivilegeModel>> getPrivilegesByRole(@PathVariable("roleId") Integer roleId) {
        return ResponseEntity.ok(privilegeService.getPrivilegesByRoleId(roleId));
    }

    /**
     * Save or update privileges for a specific role
     */
    @PostMapping("/save/{roleId}")
    public ResponseEntity<Map<String, String>> savePrivilegesForRole(
            @PathVariable("roleId") Integer roleId,
            @RequestBody List<PrivilegeDto> privilegeDtos) {
        String result = privilegeService.savePrivilegesForRole(roleId, privilegeDtos);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
