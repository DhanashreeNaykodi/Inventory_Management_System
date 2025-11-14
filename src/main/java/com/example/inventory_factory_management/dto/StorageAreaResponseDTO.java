package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageAreaResponseDTO {

    private Long id;
    private String bucket;
    private Integer rowNum;
    private Integer colNum;
    private Integer stack;
    private String factoryName;
}
