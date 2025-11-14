package com.example.inventory_factory_management.dto;
import com.example.inventory_factory_management.constants.AccountStatus;
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
    private String name;
    private String city;
    private String address;
    private AccountStatus status;


    // Use plantHead as the manager
    private UserDTO plantHead; // This acts as both plantHead and manager
    private Long plantHeadId; // For updates

    private List<UserDTO> workers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}