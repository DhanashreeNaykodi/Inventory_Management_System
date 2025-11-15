package com.example.inventory_factory_management.dto;

import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ToolRequestDTO {

    private List<Long> toolIds;
    private List<Integer> quantities;
}
