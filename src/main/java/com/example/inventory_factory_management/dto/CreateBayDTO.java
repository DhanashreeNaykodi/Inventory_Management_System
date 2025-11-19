package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBayDTO {

    @NotBlank(message = "Bay name is required")
    @Size(min = 4, max = 15, message = "Bay should be between 4 and 15 size")
    private String name;

//    private Long factoryId;
}