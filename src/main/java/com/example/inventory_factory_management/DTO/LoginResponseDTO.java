package com.example.inventory_factory_management.DTO;


import com.example.inventory_factory_management.constants.Role;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private Role role;
}
