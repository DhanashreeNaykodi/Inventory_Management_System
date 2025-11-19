package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.validations.ValidImage;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddNewToolDTO {

//    private String name;
//    private Long categoryId;
//    private ToolType type;
//    private Expensive isExpensive;
//    private Integer threshold;
//    private Integer quantity; // Only for PLANT_HEAD
//    private Long storageAreaId;//Only for PLANT_HEAD
//    private MultipartFile image;

    @NotBlank(message = "Tool name is required")
    @Size(min = 2, max = 100, message = "Tool name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Tool type is required")
    private ToolType type;

    @NotNull(message = "Expensive flag is required")
    private Expensive isExpensive;

    @NotNull(message = "Threshold is required")
    @Min(value = 1, message = "Threshold must be at least 1")
    @Max(value = 10000, message = "Threshold cannot exceed 10000")
    private Integer threshold;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    @Positive(message = "Qty must be positive")
    private Integer quantity;

    @Positive(message = "Storage area ID must be positive")
    private Long storageAreaId;

//    @ValidImage
    @NotNull(message = "Tool image is required")
    private MultipartFile image;
}
