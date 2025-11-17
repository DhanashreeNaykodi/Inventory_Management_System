package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDTO {

    private Long batchId;
//    private Integer batchNumber;
    private Integer quantityInBatch;
    private OrderStatus status;

}
