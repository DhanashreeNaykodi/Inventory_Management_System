package com.example.inventory_factory_management.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ManagerUpdateRequest {
    private Long managerId;        // For existing manager
    private UserDTO managerDetails; // For creating new manager
}