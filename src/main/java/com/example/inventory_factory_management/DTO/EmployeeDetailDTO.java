package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailDTO {
    private Long userId;
    private String username;
    private String email;
    private String img;
    private String phone;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private List<FactoryInfoDTO> factories;
}