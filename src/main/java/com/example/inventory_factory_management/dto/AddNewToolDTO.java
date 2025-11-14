package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddNewToolDTO {

    private String name;
    private Long categoryId;
    private ToolType type;
    private Expensive isExpensive;
    private Integer threshold;
    private Integer quantity; // Only for PLANT_HEAD
    private Long storageAreaId;//Only for PLANT_HEAD
    private MultipartFile image;
}
