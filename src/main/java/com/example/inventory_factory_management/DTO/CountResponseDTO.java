package com.example.inventory_factory_management.DTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CountResponseDTO {

    private Long count;
    private String entityType;

    public static CountResponseDTO of(Long count, String entityType) {
        return new CountResponseDTO(count, entityType);
    }
}
