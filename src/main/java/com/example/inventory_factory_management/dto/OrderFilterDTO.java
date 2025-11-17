package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SecondaryRow;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @SecondaryRow
public class OrderFilterDTO {

    private Long distributorId;
    private OrderStatus status;
    private String distributorName;
}
