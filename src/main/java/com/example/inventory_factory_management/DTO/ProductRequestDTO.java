package com.example.inventory_factory_management.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String prodDescription;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Reward points cannot be null")
    @Positive(message = "Reward points must be positive")
    private Integer rewardPts;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}