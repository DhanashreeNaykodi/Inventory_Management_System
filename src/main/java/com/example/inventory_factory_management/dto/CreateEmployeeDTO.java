package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateEmployeeDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")

    private String email;

    @NotBlank(message = "Phone number is required")
//    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    private Long bayId;

    private Long factoryId;

}