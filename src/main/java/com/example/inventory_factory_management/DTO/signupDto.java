package com.example.inventory_factory_management.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class signupDto {

    @Column(name = "name")
    private String manager_name;


    @Column(name = "email", unique = true)
    @NotBlank(message = "Email cannot be empty")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email must start with a letter and be valid like example@gmail.com")
    private String email;

//    private String image;

    @Column(name = "phone", unique = true)
    @NotBlank(message = "Phone no cannot be empty")
    @Pattern(regexp = "^(\\+91|0)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number")
    private Long phone;

}
