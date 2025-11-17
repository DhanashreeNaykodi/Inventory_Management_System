package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.ToolOrProductRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkerToolResponseDTO {

    private Long id;
    private Long toolId;
    private String toolName;
    private String toolType;
    private String isExpensive;
    private Long requestQty;
    private ToolOrProductRequestStatus status;
    private String workerName;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;
    private String rejectionReason;
//    private LocalDateTime autoReturnDate;
}
