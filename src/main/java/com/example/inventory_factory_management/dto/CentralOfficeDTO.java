package com.example.inventory_factory_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentralOfficeDTO {
    private String location;
    private String centralOfficerHeadEmail;
    private String centralOfficerHeadName;
    private String password;
}