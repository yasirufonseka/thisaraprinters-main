 package com.example.thisaraprinters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.thisaraprinters.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http
           .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/sweetalert2/**", "/images/**", "/artwork-uploads/**").permitAll()

//                 ===== Employee Module =====
                .requestMatchers(HttpMethod.GET, "/employees/employeemodel").hasAuthority("Employee_VIEW")
                .requestMatchers(HttpMethod.GET, "/employees/get/**").hasAuthority("Employee_VIEW")
                .requestMatchers(HttpMethod.GET, "/employees/getemployees").hasAuthority("Employee_VIEW")
                .requestMatchers(HttpMethod.POST, "/employees/add/**").hasAuthority("Employee_INSERT")
                .requestMatchers(HttpMethod.PUT, "/employees/update/**").hasAuthority("Employee_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/employees/delete/**").hasAuthority("Employee_DELETE")
                .requestMatchers("/employees/**").hasAuthority("Employee_VIEW") 

                // ===== Order Module =====
                .requestMatchers(HttpMethod.GET, "/order/management").hasAuthority("Order_VIEW")
                .requestMatchers(HttpMethod.GET, "/order/getall/**").hasAuthority("Order_VIEW")
                .requestMatchers(HttpMethod.POST, "/order/add/**").hasAuthority("Order_INSERT")
                .requestMatchers(HttpMethod.POST, "/order/update/**").hasAuthority("Order_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/order/delete/**").hasAuthority("Order_DELETE")
                .requestMatchers("/order/**").hasAuthority("Order_VIEW")

                // ===== Supplier Module =====
                .requestMatchers(HttpMethod.GET, "/supplier/**").hasAuthority("Supplier_VIEW")
                .requestMatchers(HttpMethod.POST, "/supplier/add/**").hasAuthority("Supplier_INSERT")
                .requestMatchers(HttpMethod.POST, "/supplier/update/**", "/supplier/purchaseorder/update/**").hasAuthority("Supplier_UPDATE")
                .requestMatchers(HttpMethod.POST, "/supplier/purchaseorder/**").hasAnyAuthority("Supplier_INSERT", "Supplier_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/supplier/delete/**", "/supplier/purchaseorder/delete/**").hasAuthority("Supplier_DELETE")
                .requestMatchers("/supplier/**").hasAuthority("Supplier_VIEW")

                // ===== Inventory Module =====
                .requestMatchers(HttpMethod.GET, "/inventory/**").hasAuthority("Inventory_VIEW")
                .requestMatchers(HttpMethod.POST, "/inventory/add/**").hasAuthority("Inventory_INSERT")
                .requestMatchers(HttpMethod.POST, "/inventory/update/**").hasAuthority("Inventory_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/inventory/delete/**").hasAuthority("Inventory_DELETE")
                .requestMatchers("/inventory/**").hasAuthority("Inventory_VIEW")

                // ===== Production Module =====
                .requestMatchers(HttpMethod.GET, "/production/**").hasAuthority("Production_VIEW")
                .requestMatchers(HttpMethod.POST, "/production/add/**").hasAuthority("Production_INSERT")
                .requestMatchers(HttpMethod.POST, "/production/update/**").hasAuthority("Production_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/production/delete/**").hasAuthority("Production_DELETE")
                .requestMatchers("/production/**").hasAuthority("Production_VIEW")

                // ===== Customer Module =====
                .requestMatchers(HttpMethod.GET, "/customer/**").hasAuthority("Customer_VIEW")
                .requestMatchers(HttpMethod.POST, "/customer/add/**").hasAuthority("Customer_INSERT")
                .requestMatchers(HttpMethod.POST, "/customer/update/**").hasAuthority("Customer_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/customer/delete/**").hasAuthority("Customer_DELETE")
                .requestMatchers("/customer/**").hasAuthority("Customer_VIEW")

                // ===== Quotation Module =====
                .requestMatchers(HttpMethod.GET, "/quotation/**").hasAuthority("Quotation_VIEW")
                .requestMatchers(HttpMethod.POST, "/quotation/add/**").hasAuthority("Quotation_INSERT")
                .requestMatchers(HttpMethod.POST, "/quotation/update/**").hasAuthority("Quotation_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/quotation/delete/**").hasAuthority("Quotation_DELETE")
                .requestMatchers("/quotation/**").hasAuthority("Quotation_VIEW")

                // ===== User Management Module =====
                .requestMatchers("/user/**").hasAuthority("User_VIEW")
                .requestMatchers("/privilege/**").hasAuthority("User_VIEW")

                // ===== Reports Module =====
                .requestMatchers("/reports/**").authenticated()

                // ===== Payment Module =====
                .requestMatchers(HttpMethod.GET, "/payment/management").authenticated()
                .requestMatchers(HttpMethod.GET, "/payment/supplier-orders").hasAuthority("Supplier_VIEW")
                .requestMatchers(HttpMethod.POST, "/payment/supplier/**").hasAuthority("Supplier_UPDATE")
                .requestMatchers(HttpMethod.GET, "/payment/customer-payments").hasAuthority("Customer_VIEW")
                .requestMatchers(HttpMethod.GET, "/payment/invoice/**").hasAuthority("Customer_VIEW")
                .requestMatchers(HttpMethod.POST, "/payment/customer/**").hasAnyAuthority("Customer_INSERT", "Customer_UPDATE")

                    //Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
