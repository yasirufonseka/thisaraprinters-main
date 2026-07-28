package com.example.thisaraprinters.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thisaraprinters.dto.PrivilegeDto;
import com.example.thisaraprinters.model.Module;
import com.example.thisaraprinters.model.PrivilegeModel;
import com.example.thisaraprinters.model.RoleModel;
import com.example.thisaraprinters.repository.ModuleRepo;
import com.example.thisaraprinters.repository.PrivilegeRepo;
import com.example.thisaraprinters.repository.RoleRepo;

@Service
@Transactional
public class PrivilegeService {

    private final PrivilegeRepo privilegeRepo;
    private final RoleRepo roleRepo;
    private final ModuleRepo moduleRepo;

    // All available modules in the system
    public static final List<String> ALL_MODULES = Arrays.asList(
        "Employee", "Supplier", "Inventory", "Production",
        "Order", "Customer", "User", "Quotation"
    );

    public PrivilegeService(PrivilegeRepo privilegeRepo, RoleRepo roleRepo, ModuleRepo moduleRepo) {
        this.privilegeRepo = privilegeRepo;
        this.roleRepo = roleRepo;
        this.moduleRepo = moduleRepo;
    }

    /**
     * Get all module names available in the system
     */
    public List<String> getAllModules() {
        return ALL_MODULES;
    }

    /**
     * Get all privileges assigned to a specific role
     */
    public List<PrivilegeModel> getPrivilegesByRoleId(Integer roleId) {
        RoleModel role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return role.getPrivileges() != null ? role.getPrivileges() : new ArrayList<>();
    }

    /**
     * Save or update privileges for a specific role.
     * Receives a list of PrivilegeDto (one per module) and persists them.
     *
     * Strategy:
     *  1. Clear the managed collection  → orphanRemoval queues DELETEs for all existing rows.
     *  2. saveAndFlush                  → executes the DELETEs and evicts those entities from
     *                                     the persistence context before we insert new ones.
     *  3. Build fresh PrivilegeModel    → add to the now-empty collection; JPA INSERTs them.
     *
     * This avoids the DuplicateKeyException that occurs when findById() returns a cached
     * instance that is simultaneously queued for orphan deletion.
     */
    public String savePrivilegesForRole(Integer roleId, List<PrivilegeDto> privilegeDtos) {
        RoleModel role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Step 1: Clear the managed collection (triggers orphan DELETE for every row)
        List<PrivilegeModel> existingPrivileges = role.getPrivileges();
        if (existingPrivileges != null) {
            existingPrivileges.clear();
        } else {
            role.setPrivileges(new ArrayList<>());
        }

        // Step 2: Flush so DELETEs run now, clearing the persistence context cache
        roleRepo.saveAndFlush(role);

        // Step 3: Build and attach fresh PrivilegeModel objects
        for (PrivilegeDto dto : privilegeDtos) {
            Module moduleEntity = moduleRepo.findByName(dto.getModule());
            if (moduleEntity == null) {
                throw new RuntimeException("Module not found: " + dto.getModule());
            }

            PrivilegeModel privilege = new PrivilegeModel();
            privilege.setRole(role);
            privilege.setModule(moduleEntity);
            privilege.setCanInsert(dto.getCanInsert() != null ? dto.getCanInsert() : false);
            privilege.setCanUpdate(dto.getCanUpdate() != null ? dto.getCanUpdate() : false);
            privilege.setCanDelete(dto.getCanDelete() != null ? dto.getCanDelete() : false);
            privilege.setCanView(dto.getCanView() != null ? dto.getCanView() : false);

            role.getPrivileges().add(privilege);
        }

        roleRepo.save(role);
        return "Privileges saved successfully for role: " + role.getName();
    }

    /**
     * Check if a user (via their roles) has a specific permission on a module
     */
    public boolean hasPermission(List<RoleModel> roles, String module, String action) {
        if (roles == null) return false;

        for (RoleModel role : roles) {
            if (role.getPrivileges() == null) continue;

            for (PrivilegeModel privilege : role.getPrivileges()) {
                if (privilege.getModule().getName().equalsIgnoreCase(module)) {
                    switch (action.toLowerCase()) {
                        case "insert": return Boolean.TRUE.equals(privilege.getCanInsert());
                        case "update": return Boolean.TRUE.equals(privilege.getCanUpdate());
                        case "delete": return Boolean.TRUE.equals(privilege.getCanDelete());
                        case "view": return Boolean.TRUE.equals(privilege.getCanView());
                    }
                }
            }
        }
        return false;
    }
}
