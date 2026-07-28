package com.example.thisaraprinters.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerDto {
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Company contact is required")
    @Pattern(regexp = "^0\\d{9}$", message = "Company contact must be a valid 10-digit Sri Lankan phone number starting with 0")
    private String phone;

    private String contactperson;

    @Pattern(regexp = "^(0\\d{9})?$", message = "Contact person contact must be a valid 10-digit phone number starting with 0")
    private String contactpersonphone;
}

