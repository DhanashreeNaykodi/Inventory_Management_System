package com.example.inventory_factory_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFilterDTO {
    private String search;      // Search by username/name
    private String role;        // Filter by role
    private Long factoryId;     // Filter by factory
    private Integer page;
    private Integer size;
}