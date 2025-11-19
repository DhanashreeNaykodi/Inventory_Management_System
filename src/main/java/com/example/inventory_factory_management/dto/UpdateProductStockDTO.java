package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductStockDTO {

    private Long productId;

    //    @Positive(message = "Quantity should be positive")
    private Integer quantity;
}
