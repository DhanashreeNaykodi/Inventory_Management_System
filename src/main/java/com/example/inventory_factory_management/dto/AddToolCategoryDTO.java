package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddToolCategoryDTO {

    @NotBlank(message = "Name is required")
    @Size(min=4, max = 20, message = "Tool category name should be in range 4 to 20")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min=4, max = 70, message = "Tool category description should be in range 4 to 70")
    private String description;
}
