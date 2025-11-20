package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.validations.ValidImage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateToolDTO {

    @NotBlank(message = "Tool name is required")
    @Size(min = 3, max = 20, message = "Tool name should be between 3 to 20 length")
    private String name;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Id should be positive")
    private Long categoryId;

//    @NotBlank(message = "Tool type is required")
    private ToolType type;

//    @NotBlank(message = "Expense type is required")
    private Expensive isExpensive;

    @NotNull(message = "Threshold is required")
    private Integer threshold;

    @ValidImage
    private MultipartFile image;
}