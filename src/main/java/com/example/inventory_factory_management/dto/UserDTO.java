package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Long userId;

//    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 20, message = "Username must be in 4 to 20 range")
    private String username;

//    @NotBlank(message = "Email is required")
    @Size(max = 40, message = "Username must be in 4 to 20 range")
    @Email(message = "Enter valid email")
    private String email;

    private String img;
    private MultipartFile profileImage; // For file upload
    private Role role;

//    @NotBlank(message = "Phone cannot be null")
    @Size(min = 10, max = 10, message = "Phone has to be of 10 digits")
    private String phone;


    private String password;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For factory assignments
    private Long factoryId;
    private String factoryName;
    private Role factoryRole;
}

