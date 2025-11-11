package com.example.inventory_factory_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddChiefOfficerDTO {
    private Long centralOfficeId;
    private String centralOfficerEmail;
    private String centralOfficerName;
    private String phone;
}