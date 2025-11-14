package com.example.inventory_factory_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CentralOfficerDTO {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Long officeId;
    private String officeName;
    private String status;
}