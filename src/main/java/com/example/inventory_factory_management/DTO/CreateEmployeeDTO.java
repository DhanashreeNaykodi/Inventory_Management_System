package com.example.inventory_factory_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeDTO {
    private String username;
    private String email;
    private String phone;
    private String role; // WORKER or CHIEF_SUPERVISOR
    private Long bayId; // Only for workers
    private Long factoryId; // Factory where employee will work
}