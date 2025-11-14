package com.example.inventory_factory_management.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String username;
    private String email;
    private String phone;
    private String img; // For existing image URL
}