package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentralOfficeRestockResponseDTO {
    private Long id;
    private Long factoryId;
    private String factoryName;
    private Long productId;
    private String productName;
    private Integer qtyRequested;
    private RequestStatus status;
    private LocalDateTime createdAt;
//    private Long createdByUserId;
//    private String requestedByUserName;
    private LocalDateTime completedAt;

    // NEW: Factory manager information (from factory assignment)
//    private Long managerUserId;
//    private String managerUserName;

    // Factory-level information
    private Long currentFactoryStock;

    // Central office information (only for central officers/owners)
    private Long centralOfficeStock;
}