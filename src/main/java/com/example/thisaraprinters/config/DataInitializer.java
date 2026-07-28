package com.example.thisaraprinters.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.thisaraprinters.model.DesignationModel;
import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.model.Module;
import com.example.thisaraprinters.model.PrivilegeModel;
import com.example.thisaraprinters.model.RoleModel;
import com.example.thisaraprinters.model.UserModel;
import com.example.thisaraprinters.repository.DesignationRepo;
import com.example.thisaraprinters.repository.EmployeeRepo;
import com.example.thisaraprinters.repository.ModuleRepo;
import com.example.thisaraprinters.repository.PrivilegeRepo;
import com.example.thisaraprinters.repository.RoleRepo;
import com.example.thisaraprinters.repository.UserRepo;
import com.example.thisaraprinters.service.PrivilegeService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PrivilegeRepo privilegeRepo;
    private final EmployeeRepo employeeRepo;
    private final DesignationRepo designationRepo;
    private final ModuleRepo moduleRepo;
   // private final PasswordEncoder passwordEncoder;
//PasswordEncoder passwordEncoder
    public DataInitializer(UserRepo userRepo, RoleRepo roleRepo, PrivilegeRepo privilegeRepo, 
                           EmployeeRepo employeeRepo, DesignationRepo designationRepo, 
                           ModuleRepo moduleRepo ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.privilegeRepo = privilegeRepo;
        this.employeeRepo = employeeRepo;
        this.designationRepo = designationRepo;
        this.moduleRepo = moduleRepo;
       // this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Create Admin Role if not exists
        RoleModel adminRole = null;
        List<RoleModel> allRoles = roleRepo.findAll();
        for (RoleModel role : allRoles) {
            if ("Admin".equalsIgnoreCase(role.getName())) {
                adminRole = role;
                break;
            }
        }

        if (adminRole == null) {
            adminRole = new RoleModel();
            adminRole.setName("Admin");
            adminRole = roleRepo.save(adminRole);
            System.out.println("Default 'Admin' role created.");
        }

        // Ensure all modules exist
        for (int i = 0; i < PrivilegeService.ALL_MODULES.size(); i++) {
            String moduleName = PrivilegeService.ALL_MODULES.get(i);
            if (moduleRepo.findByName(moduleName) == null) {
                Module module = new Module();
                module.setId(i + 1);
                module.setName(moduleName);
                moduleRepo.save(module);
            }
        }

        // 2. Setup Full Privileges for Admin Role (only if not already setup)
        if (privilegeRepo.count() == 0) {
            List<PrivilegeModel> adminPrivileges = new ArrayList<>();
            for (String moduleName : PrivilegeService.ALL_MODULES) {
                Module module = moduleRepo.findByName(moduleName);
                if (module != null) {
                    PrivilegeModel priv = new PrivilegeModel();
                    priv.setRole(adminRole);
                    priv.setModule(module);
                    priv.setCanView(true);
                    priv.setCanInsert(true);
                    priv.setCanUpdate(true);
                    priv.setCanDelete(true);
                    privilegeRepo.save(priv);
                    adminPrivileges.add(priv);
                }
            }
            adminRole.setPrivileges(adminPrivileges);
            roleRepo.save(adminRole);
            System.out.println("Default admin privileges created.");
        }

        // 3. Seed default designations (Manager, Designer, Operator)
        String[] defaultDesignations = {"Manager", "Designer", "Operator"};
        List<DesignationModel> existingDesignations = designationRepo.findAll();
        java.util.Set<String> existingDesignationNames = new java.util.HashSet<>();
        for (DesignationModel d : existingDesignations) {
            existingDesignationNames.add(d.getDesignation());
        }
        for (String desigName : defaultDesignations) {
            if (!existingDesignationNames.contains(desigName)) {
                DesignationModel newDesig = new DesignationModel();
                newDesig.setDesignation(desigName);
                designationRepo.save(newDesig);
                System.out.println("Default designation created: " + desigName);
            }
        }

        // 4. Ensure a default Employee and User exist for admin login
        UserModel adminUser = userRepo.findByUsername("admin");
        if (adminUser == null) {
            // Need an employee first
            EmployeeModel adminEmployee;
            List<EmployeeModel> allEmployees = employeeRepo.findAll();
            if (allEmployees.isEmpty()) {
                // Get Manager designation for admin employee
                DesignationModel adminDesignation;
                List<DesignationModel> allDesignations = designationRepo.findAll();
                adminDesignation = allDesignations.stream()
                    .filter(d -> "Manager".equals(d.getDesignation()))
                    .findFirst()
                    .orElse(allDesignations.get(0));

                adminEmployee = new EmployeeModel();
                adminEmployee.setFullname("System Administrator");
                adminEmployee.setCallingname("Admin");
                adminEmployee.setNic("000000000V"); // Correct field name is 'nic'
                adminEmployee.setDesignationid(adminDesignation);
                adminEmployee.setAddeddate(LocalDate.now());
                adminEmployee = employeeRepo.save(adminEmployee);
                System.out.println("Default admin employee created.");
            } else {
                adminEmployee = allEmployees.get(0);
            }

            adminUser = new UserModel();
            adminUser.setUsername("admin");
            adminUser.setEmployeeid(adminEmployee);
            adminUser.setStatus("Active");
            adminUser.setAddeddate(LocalDate.now());
            System.out.println("Default admin user created: admin / admin123");
        } else {
            System.out.println("Admin user found. Resetting password and roles to ensure access.");
        }

//        adminUser.setPassword(passwordEncoder.encode("admin123"));
//        adminUser.setRole(adminRole);
       // userRepo.save(adminUser);
    }
}
