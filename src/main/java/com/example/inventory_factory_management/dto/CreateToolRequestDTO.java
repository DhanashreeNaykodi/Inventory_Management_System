package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateToolRequestDTO {

        @Positive(message = "Id should be positive")
    @NotBlank(message = "Id cannot be blank")
    private Long toolId;


        @Positive(message = "Id should be positive")
    @NotBlank(message = "Id cannot be blank")
    private Integer quantity;
}
