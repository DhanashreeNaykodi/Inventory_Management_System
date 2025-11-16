package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolStorageDetailDTO {
    private Long toolId;
    private String toolName;
    private String toolCategory;
    private String toolType;
    private String imageUrl;
    private Long totalQuantityInFactory; // From ToolStock
    private Long availableQuantity;      // From ToolStock
    private List<StorageLocationDetail> storageLocations; // Where this tool is stored


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StorageLocationDetail {
        private String locationCode;
        private Integer rowNum;
        private Integer colNum;
        private Integer stack;
        private String bucket;
        private Integer quantity;
    }
}