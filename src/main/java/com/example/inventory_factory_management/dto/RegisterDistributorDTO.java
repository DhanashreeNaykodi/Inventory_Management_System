package com.example.inventory_factory_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDistributorDTO {

    private String name;
    private String email;
//    private String password;  //later remove coz password will be auto generated and sent via email
    private String companyName;
    private String contactNumber;
    private String address;
}
