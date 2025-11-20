package com.example.inventory_factory_management.dto;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FactoryDTO {
    private Long factoryId;

    @NotBlank(message = "Name is required")
    @Size(min = 4, max = 20, message = "Name should be between 4 to 20")
    private String name;

    @NotBlank(message = "City is required")
    @Size(min = 3, max = 20, message = "City name should be between 4 to 20")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(min = 3, max = 40, message = "Address should be between 8 to 40")
    private String address;

    private AccountStatus status;


    // Use plantHead as the manager
    private UserDTO plantHead; // This acts as both plantHead and manager

    @NotNull(message = "Plant head id is required")
    private Long plantHeadId; // For updates

    private List<UserDTO> workers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}