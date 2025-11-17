package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.OrderStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderActionDTO {
    private OrderStatus status;
    private String rejectReason;
}
