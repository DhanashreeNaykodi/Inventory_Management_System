package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 50, message = "User name must be between 3 and 50 characters")
    private String username;

//    @Pattern(
//            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
//            message = "Email must start with a letter and be valid like example@gmail.com")

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Enter valid email")
    private String email;

//  @NotBlank(message = "Contact number cannot be blank")
//  @Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$", message = "Enter a valid Indian mobile number")
//     private Long phone;

    @NotBlank(message = "Phone cannot be null")
//    @Size(min = 10, max = 10, message = "Phone has to be of 10 digits")
    private String phone;

}