package com.example.thisaraprinters.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.thisaraprinters.model.PrivilegeModel;
import com.example.thisaraprinters.model.RoleModel;
import com.example.thisaraprinters.model.UserModel;
import com.example.thisaraprinters.repository.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Check if user is active
        if (!"Active".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("User account is inactive: " + username);
        }

        return new User(
            user.getUsername(),
            user.getPassword(),
            getAuthorities(user.getRole())
        );
    }

    /**
     * Build authorities from the role and its module privileges.
     * Creates authorities like:
     *   ROLE_Admin, ROLE_StoreKeeper
     *   Employee_INSERT, Employee_UPDATE, Employee_VIEW
     *   Supplier_INSERT, Supplier_VIEW
     */
    private Collection<? extends GrantedAuthority> getAuthorities(RoleModel role) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role == null) return authorities;

        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        // Add module-privilege-based authorities
        if (role.getPrivileges() != null) {
            for (PrivilegeModel privilege : role.getPrivileges()) {
                String module = privilege.getModule().getName();
                if (Boolean.TRUE.equals(privilege.getCanInsert())) {
                    authorities.add(new SimpleGrantedAuthority(module + "_INSERT"));
                }
                if (Boolean.TRUE.equals(privilege.getCanUpdate())) {
                    authorities.add(new SimpleGrantedAuthority(module + "_UPDATE"));
                }
                if (Boolean.TRUE.equals(privilege.getCanDelete())) {
                    authorities.add(new SimpleGrantedAuthority(module + "_DELETE"));
                }
                if (Boolean.TRUE.equals(privilege.getCanView())) {
                    authorities.add(new SimpleGrantedAuthority(module + "_VIEW"));
                }
            }
        }

        return authorities;
    }
}
