package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Expensive;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToolStockResponseDTO {

    private Long id;
    private Long toolId;
    private String toolName;
    private String toolCategory;
    private String toolType;
    private Expensive isExpensive;
    private String imageUrl;
    private Long totalQuantity;
    private Long availableQuantity;
    private Long issuedQuantity;
    private LocalDateTime lastUpdatedAt;
}