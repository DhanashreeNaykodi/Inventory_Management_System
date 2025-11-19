package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryProductCountDTO {
    private Long factoryId;
    private String factoryName;
    private Long productId;
    private String productName;
    private Long productCount;
}