package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String username;
    private String email;
    private String phone;
    private String img; // For existing image URL
}