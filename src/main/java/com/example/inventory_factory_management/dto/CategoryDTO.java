package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 3, max = 20, message = "Category name should be between 3 to 20 length")
    private String categoryName;


    @NotBlank(message = "Category description cannot be blank")
    @Size(min = 3, max = 50, message = "Category description should be between 3 to 50 length")
    private String description;

    private Integer productCount;
}