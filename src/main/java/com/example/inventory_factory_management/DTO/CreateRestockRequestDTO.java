package com.example.inventory_factory_management.DTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRestockRequestDTO {
    private String factoryName;
    private Long productId;
    private Integer qtyRequested;
}