package com.example.thisaraprinters.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserDto {
    
    private String username;
    private String password;
    private LocalDate addeddate;
    private LocalDate updateddate;
    private String note;
    private String userphoto;
    private String status;
    private Long employeeid;
    private Integer roleId;
    private String email;

}
