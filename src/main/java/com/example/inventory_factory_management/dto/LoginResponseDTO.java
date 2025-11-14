package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.Role;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private Role role;
}
