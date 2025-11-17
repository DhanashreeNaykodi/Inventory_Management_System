package com.example.inventory_factory_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributorOrderRequestDTO {
    private List<OrderItemRequestDTO> orderItems;
}