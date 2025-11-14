package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Expensive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolResponseDTO {
    private Long id;
    private String name;
    private String categoryName;
    private String type;
    private Expensive isExpensive;
    private Integer threshold;
    private Integer quantity;
    private String storageAreaName;
    private String factoryName;
    private String image;
}