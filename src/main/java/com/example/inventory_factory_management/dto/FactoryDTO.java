package com.example.inventory_factory_management.dto;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.validation.constraints.NotBlank;
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

    //    @NotBlank(message = "Name is required")
    private String name;

//    @NotBlank(message = "City is required")
    private String city;

//    @NotBlank(message = "City is required")
    private String address;
    private AccountStatus status;


    // Use plantHead as the manager
    private UserDTO plantHead; // This acts as both plantHead and manager
    private Long plantHeadId; // For updates

    private List<UserDTO> workers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}