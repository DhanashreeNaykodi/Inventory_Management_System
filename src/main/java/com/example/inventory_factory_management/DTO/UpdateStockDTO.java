package com.example.inventory_factory_management.DTO;


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
