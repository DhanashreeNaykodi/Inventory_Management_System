package com.example.inventory_factory_management.DTO;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBayDTO {
    private String name;
    private Long factoryId;
}