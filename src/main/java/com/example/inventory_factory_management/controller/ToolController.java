package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.dto.AddToolCategoryDTO;
import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.ToolCategoryDTO;
import com.example.inventory_factory_management.service.ToolCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {


    @Autowired
    private ToolCategoryService toolCategoryService;


    @GetMapping("/tool-categories")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<BaseResponseDTO<Page<ToolCategoryDTO>>> getAllToolCategoriesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        BaseResponseDTO<Page<ToolCategoryDTO>> response = toolCategoryService.getAllToolCategories(pageable);

        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }


    @PostMapping("/tool-categories/create")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public BaseResponseDTO<ToolCategoryDTO> createToolCategory(@RequestBody AddToolCategoryDTO addToolCategoryDTO) {
        BaseResponseDTO<ToolCategoryDTO> response = toolCategoryService.createToolCategory(addToolCategoryDTO);
        return response;
    }


    @PutMapping("/tool-categories/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<BaseResponseDTO<ToolCategoryDTO>> updateToolCategory(
            @PathVariable Long id,
            @RequestBody AddToolCategoryDTO addToolCategoryDTO) {

        BaseResponseDTO<ToolCategoryDTO> response = toolCategoryService.updateToolCategory(id, addToolCategoryDTO);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }


    @DeleteMapping("/tool-categories/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<BaseResponseDTO<Void>> deleteToolCategory(@PathVariable Long id) {
        BaseResponseDTO<Void> response = toolCategoryService.deleteToolCategory(id);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

}