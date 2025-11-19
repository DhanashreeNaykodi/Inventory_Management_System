package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemRequestDTO {

    @NotNull(message = "ID is required")
    @Positive(message = "Id should be positive")
    private Long productId;

    @NotNull(message = "ID is required")
    @Positive(message = "Id should be positive")
    private Integer quantity;
}
