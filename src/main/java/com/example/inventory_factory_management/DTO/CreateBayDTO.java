package com.example.inventory_factory_management.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBayDTO {

    @NotBlank(message = "Bay name is required")
    private String name;

//    private Long factoryId;
}