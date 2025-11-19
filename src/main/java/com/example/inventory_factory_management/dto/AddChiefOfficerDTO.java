package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddChiefOfficerDTO {


    @NotNull(message = "Central office ID is required")
    private Long centralOfficeId;

    @NotBlank(message = "Email is required")
//    @Pattern(
//            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
//            message = "Email must start with a letter and be valid like example@gmail.com")
    @Email(message = "Enter valid email")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String centralOfficerEmail;

    @NotBlank(message = "Officer name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name can only contain letters and spaces")
    private String centralOfficerName;

    @NotBlank(message = "Phone number is required")
//    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number format")
//    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    private String phone;
}