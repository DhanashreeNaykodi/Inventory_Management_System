package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRestockRequestDTO {

    @NotNull(message = "factory id is required")
    private Long factoryId;

    @NotNull(message = "product id is required")
    private Long productId;

    @NotNull(message = "quantity id is required")
    private Integer qtyRequested;
}