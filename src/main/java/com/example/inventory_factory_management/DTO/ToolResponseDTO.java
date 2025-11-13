package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.expensive;
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
    private expensive isExpensive;
    private Integer threshold;
    private Integer quantity;
    private String storageAreaName;
    private String factoryName;
}