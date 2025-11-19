package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "Email is required")
    @Email
    String email;

    @NotBlank(message = "Password is required")
    String password;
}
