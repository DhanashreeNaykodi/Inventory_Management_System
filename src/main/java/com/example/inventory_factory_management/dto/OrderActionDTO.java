package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderActionDTO {

//    @NotNull(message = )
    private OrderStatus status;

    @NotNull(message = "Reason is needed.")
    private String rejectReason;
}
