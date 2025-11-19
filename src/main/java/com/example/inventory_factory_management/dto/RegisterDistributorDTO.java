package com.example.inventory_factory_management.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDistributorDTO {

    @NotBlank(message = "Name cannot be blank")
        @Size(min = 3, max = 50, message = "Distributor name must be between 3 and 50 characters")
    private String name;


//    @NotBlank(message = "Email cannot be blank")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email must start with a letter and be valid like example@gmail.com")
    private String email;


    @NotBlank(message = "Company name cannot be blank")
        @Size(min = 3, max = 50, message = "Company name must be between 3 and 50 characters")
    private String companyName;


    @NotBlank(message = "Contact number cannot be blank")
    @Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
    private Long phone;

    @NotBlank(message = "Address cannot be blank")
        @Size(min = 3, max = 50, message = "Address must be between 3 and 50 characters")
    private String address;
}
