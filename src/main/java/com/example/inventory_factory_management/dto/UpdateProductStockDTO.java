package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductStockDTO {

    @NotNull(message = "Id cannot be null")
    @Positive(message = "Id should be positive")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity should be positive")
    private Integer quantity;
}
