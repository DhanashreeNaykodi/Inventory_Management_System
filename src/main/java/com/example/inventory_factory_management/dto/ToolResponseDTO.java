package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolResponseDTO {
    private Long id;
    private String name;
    private String categoryName;
    private Long categoryId;
    private String type;
    private Expensive isExpensive;
    private Integer threshold;
    private String imageUrl;
    private AccountStatus status;
    private LocalDateTime createdAt;
}