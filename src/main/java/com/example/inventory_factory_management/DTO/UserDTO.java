package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
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
    private String username;
    private String email;
    private String img;
    private MultipartFile profileImage; // For file upload
    private Role role;
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


//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class UserDTO {
//    private Long userId;
//
//    @NotBlank(message = "Username cannot be blank")
//    private String username;
//
//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email cannot be blank")
//    private String email;
//
//    @Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$",
//            message = "Enter a valid Indian mobile number")
//    private String phone;
//}