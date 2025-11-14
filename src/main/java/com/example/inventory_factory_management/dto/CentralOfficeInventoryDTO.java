package com.example.inventory_factory_management.dto;

import lombok.*;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentralOfficeInventoryDTO {
    private Long productId;
    private String productName;
    private Long quantity;
    private Long totalReceived;
}