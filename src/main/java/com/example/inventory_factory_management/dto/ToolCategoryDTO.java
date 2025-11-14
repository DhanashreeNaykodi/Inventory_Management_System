package com.example.inventory_factory_management.dto;

import lombok.*;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToolCategoryDTO {
    private Long id;
    private String name;
    private String description;
//    private Integer toolCount;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
}