package com.example.inventory_factory_management.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssignToolToFactoryDTO {

    @NotEmpty(message = "Tool IDs list cannot be empty")
    private List<Long> tool_ids;

    @NotEmpty(message = "Quantities list cannot be empty")
    private List<Integer> quantities;

    @NotEmpty(message = "Storage locations list cannot be empty")
    private List<String> storage_locations; // List of location codes like ["R1C1S1B1", "R1C1S1B2"]








    @AssertTrue(message = "Tool IDs, Quantities and Storage Locations lists must have the same size")
    private boolean isSizesEqual() {
        return tool_ids != null &&
                quantities != null &&
                storage_locations != null &&
                tool_ids.size() == quantities.size() &&
                quantities.size() == storage_locations.size();
    }
}
