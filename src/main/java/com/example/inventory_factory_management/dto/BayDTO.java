package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BayDTO {
    @NotNull(message = "bayId must be present")
    private Long bayId;
    @NotBlank(message = "name is empty")
    private String name;
    @NotBlank(message = "descirption is empty")
    private String description;
    @NotNull(message = "factory not selected")
    private Long factoryId;

    private String factoryName;
}