package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRestockRequestDTO {

    @NotNull(message = "factory id is required")
    @Positive(message = "Factory id should be positive")
    private Long factoryId;

    @NotNull(message = "product id is required")
    @Positive(message = "Product id should be positive")
    private Long productId;

    @NotNull(message = "quantity id is required")
    @Positive(message = "Quantity should be positive")
    private Integer qtyRequested;
}