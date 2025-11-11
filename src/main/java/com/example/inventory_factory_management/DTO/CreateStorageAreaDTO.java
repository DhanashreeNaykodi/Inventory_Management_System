package com.example.inventory_factory_management.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateStorageAreaDTO {
    private Integer rowNum;
    private Integer colNum;
    private Integer stack;
}