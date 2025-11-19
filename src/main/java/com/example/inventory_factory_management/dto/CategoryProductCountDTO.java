package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryProductCountDTO {
    private Long categoryId;
    private String categoryName;
    private Long productCount;
}