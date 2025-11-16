package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageAreaDTO {

    private Long id;
    private String locationCode;
    private Integer rowNum;
    private Integer colNum;
    private Integer stack;
    private String bucket;
    private Long factoryId;
    private String factoryName;
}
