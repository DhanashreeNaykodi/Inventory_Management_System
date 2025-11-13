package com.example.inventory_factory_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignToolToFactoryDTO {

    private Long toolId;
    private Long storageAreaId;
    private Integer quantity;
}
