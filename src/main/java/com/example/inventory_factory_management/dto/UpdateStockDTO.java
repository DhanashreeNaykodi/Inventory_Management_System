package com.example.inventory_factory_management.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateStockDTO {

    private Long productId;
    private Integer quantity;
}
