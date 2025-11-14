package com.example.inventory_factory_management.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRestockRequestDTO {
    private Long factoryId;
    private Long productId;
    private Integer qtyRequested;
}