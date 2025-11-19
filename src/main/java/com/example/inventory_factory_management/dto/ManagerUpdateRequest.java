package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ManagerUpdateRequest {

    @Positive(message = "Id should be positive")
    @NotNull(message = "Id should be positive")
    private Long managerId;        // For existing manager

    @NotBlank(message = "Manager details cannot be empty")
    private UserDTO managerDetails; // For creating new manager
}