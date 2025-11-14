package com.example.inventory_factory_management.dto;

import lombok.Data;

@Data
public class BaseRequestDTO {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}