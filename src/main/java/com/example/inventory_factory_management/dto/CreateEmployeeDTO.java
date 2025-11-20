package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateEmployeeDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 20, message = "Username should be in between 2 and 20 characters")
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