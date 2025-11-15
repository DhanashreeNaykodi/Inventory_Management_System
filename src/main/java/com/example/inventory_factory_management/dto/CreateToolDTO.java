package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateToolDTO {

    @NotBlank(message = "Tool name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private ToolType type;
    private Expensive isExpensive;
    private Integer threshold;
    private MultipartFile image;
}