package com.example.inventory_factory_management.DTO;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BayDTO {
    private Long bayId;
    private String name;
    private String description;
    private Long factoryId;
    private String factoryName;
}