package com.example.inventory_factory_management.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductStockDTO {

    private Long productId;
    private Integer quantity;
}
