package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileWithFactoryDTO {
    private Long userId;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 50, message = "Distributor name must be between 3 and 50 characters")
    private String username;

    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email must start with a letter and be valid like example@gmail.com")
    private String email;
    private String img;


    //    @NotBlank(message = "Contact number cannot be blank")
//    @Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$",
//            message = "Enter a valid Indian mobile number")
    private String phone;


    private Role role;
    private AccountStatus status;
    private List<FactoryInfoDTO> factories;
}