package com.example.thisaraprinters.dto;

import java.time.LocalDate;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeDto {
    @NotBlank(message = "Employee Full name can not be empty")
    private String fullname;
    private MultipartFile image;
    private String callingname;
    @NotBlank(message = "Employee Address can not be empty")
    private String address;
    @NotBlank(message = "Email can not be empty")
    private String email;
    @NotBlank(message = "Employee have a gender")
    private String gender;
    private LocalDate dob;
    @NotBlank(message = "Employee Full name can not be empty")
    private String nic;
    @NotBlank(message = "Employee Full name can not be empty")
    @Pattern(regexp = "^0\\d{9}$", message = "Employee contact must be a valid 10-digit Sri Lankan phone number starting with 0")
    private String phonenumber;
    @NotBlank(message = "Employee always should have emergency person and their cotact")
    private String emgpersonname;
    @NotBlank(message = "Employee always should have emergency person and their cotact")
    private String emgpersonphonenumber;
    private LocalDate addeddate;
    private LocalDate updateddate;
    @NotBlank(message = "Employee shold have a designation")
    private Integer designationid;

}
