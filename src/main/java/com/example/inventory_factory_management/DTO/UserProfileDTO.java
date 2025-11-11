package com.example.inventory_factory_management.DTO;


import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private String img;
    private String phone;
    private Role role;
    private account_status status;
}
