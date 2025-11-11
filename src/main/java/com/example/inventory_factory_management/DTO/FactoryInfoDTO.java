package com.example.inventory_factory_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactoryInfoDTO {
    private Long factoryId;
    private String factoryName;
    private String location;
}