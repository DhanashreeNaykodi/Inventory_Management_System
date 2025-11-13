package com.example.inventory_factory_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentralOfficeResponseDTO {
    private Long id;
    private String location;
    private String name;
    private List<UserProfileDTO> officers;
}