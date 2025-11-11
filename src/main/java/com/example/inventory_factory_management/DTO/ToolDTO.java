package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.constants.expensive;
import com.example.inventory_factory_management.constants.toolType;
import lombok.*;

import java.time.LocalDateTime;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToolDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
//    private MultipartFile image; // For file upload
    private toolType type;
    private expensive isExpensive;
    private Integer threshold;
    private Integer qty;
    private account_status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}