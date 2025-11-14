package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileWithFactoryDTO {
    private Long userId;
    private String username;
    private String email;
    private String img;
    private String phone;
    private Role role;
    private AccountStatus status;
    private List<FactoryInfoDTO> factories;
}