package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryRestockResponseDTO {
    private Long id;
    private Long factoryId;
    private String factoryName;
    private Long productId;
    private String productName;
    private Integer qtyRequested;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Long requestedByUserId;
    private String requestedByUserName;

    // NEW: Factory manager information (from factory assignment)
//    private Long managerUserId;
//    private String managerUserName;
    private LocalDateTime completedAt;

    // Add this field if you need current factory stock
    private Long currentFactoryStock;
}