package com.example.inventory_factory_management.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddToolCategoryDTO {
    private String name;
    private String description;
}
