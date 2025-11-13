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
public class EmployeeResponseDTO {
    private List<EmployeeDetailDTO> employees;
    private int totalCount;
    private Long factoryId;
    private String factoryName;
}