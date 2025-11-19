package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.dto.AddToolCategoryDTO;
import com.example.inventory_factory_management.dto.BaseRequestDTO;
import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.ToolCategoryDTO;
import com.example.inventory_factory_management.entity.ToolCategory;
import com.example.inventory_factory_management.repository.ToolCategoryRepository;
import com.example.inventory_factory_management.specifications.ToolCategorySpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ToolCategoryService {

    @Autowired
    private final ToolCategoryRepository toolCategoryRepository;


    public BaseResponseDTO<Page<ToolCategoryDTO>> getAllToolCategories(
            BaseRequestDTO request,
            String search) {

        try {
            Specification<ToolCategory> spec = ToolCategorySpecifications.withFilters(search);

            Pageable pageable = PaginationUtil.toPageable(request);

            Page<ToolCategory> categoriesPage = toolCategoryRepository.findAll(spec, pageable);
            Page<ToolCategoryDTO> categoryDTOsPage = categoriesPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Tool categories retrieved successfully", categoryDTOsPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve tool categories: " + e.getMessage());
        }
    }


    public BaseResponseDTO<ToolCategoryDTO> createToolCategory(AddToolCategoryDTO addToolCategoryDTO) {
        try {
            if (addToolCategoryDTO.getName() == null || addToolCategoryDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Tool category name is required");
            }

            String categoryName = addToolCategoryDTO.getName().trim();

            if (toolCategoryRepository.existsByNameIgnoreCase(categoryName)) {
                return BaseResponseDTO.error("Tool category with name '" + categoryName + "' already exists");
            }

            ToolCategory category = convertToEntity(addToolCategoryDTO);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());

            ToolCategory savedCategory = toolCategoryRepository.save(category);
            ToolCategoryDTO savedCategoryDTO = convertToDTO(savedCategory);
            return BaseResponseDTO.success("Tool category created successfully", savedCategoryDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create tool category: " + e.getMessage());
        }
    }


    public BaseResponseDTO<ToolCategoryDTO> updateToolCategory(Long id, AddToolCategoryDTO addToolCategoryDTO) {
        try {

            // Validate input
            if (id == null || id <= 0) {
                return BaseResponseDTO.error("Invalid tool category ID");
            }

            if (addToolCategoryDTO.getName() == null || addToolCategoryDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Tool category name is required");
            }

            String categoryName = addToolCategoryDTO.getName().trim();

            Optional<ToolCategory> categoryOptional = toolCategoryRepository.findById(id);

            if (categoryOptional.isEmpty()) {
                return BaseResponseDTO.error("Tool category not found with ID: " + id);
            }

            ToolCategory existingCategory = categoryOptional.get();

            if (!existingCategory.getName().equals(categoryName)) {
                Optional<ToolCategory> duplicateCategory = toolCategoryRepository
                        .findByNameAndIdNot(categoryName, id);
                if (duplicateCategory.isPresent()) {
                    return BaseResponseDTO.error("Another tool category with name '" + categoryName + "' already exists");
                }
            }

            existingCategory.setName(categoryName);
            existingCategory.setDescription(addToolCategoryDTO.getDescription());
            existingCategory.setUpdatedAt(LocalDateTime.now());

            ToolCategory updatedCategory = toolCategoryRepository.save(existingCategory);
            ToolCategoryDTO updatedCategoryDTO = convertToDTO(updatedCategory);

            return BaseResponseDTO.success("Tool category updated successfully", updatedCategoryDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update tool category: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Void> deleteToolCategory(Long id) {
        try {
            if (id == null || id <= 0) {
                return BaseResponseDTO.error("Invalid tool category ID");
            }

            Optional<ToolCategory> categoryOptional = toolCategoryRepository.findById(id);

            if (categoryOptional.isEmpty()) {
                return BaseResponseDTO.error("Tool category not found with ID: " + id);
            }

            ToolCategory category = categoryOptional.get();

            // Check if category has associated tools
            if (category.getTools() != null && !category.getTools().isEmpty()) {
                return BaseResponseDTO.error("Cannot delete tool category as it has associated tools. Please remove the tools first.");
            }

            toolCategoryRepository.deleteById(id);
            return BaseResponseDTO.success("Tool category deleted successfully", null);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete tool category: " + e.getMessage());
        }
    }



    // Utility methods
    private ToolCategoryDTO convertToDTO(ToolCategory category) {
        ToolCategoryDTO dto = new ToolCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    private ToolCategory convertToEntity(AddToolCategoryDTO dto) {
        ToolCategory category = new ToolCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}