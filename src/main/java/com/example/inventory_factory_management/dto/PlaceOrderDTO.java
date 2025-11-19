package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderDTO {

    @NotBlank(message = "order cannot be empty")
    private List<OrderItemRequestDTO> orderItems;
}
