package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.DTO.AddToolCategoryDTO;
import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.ToolCategoryDTO;
import com.example.inventory_factory_management.entity.ToolCategory;
import com.example.inventory_factory_management.repository.ToolCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ToolCategoryService {

    @Autowired
    private final ToolCategoryRepository toolCategoryRepository;


    @Transactional(readOnly = true)
    public BaseResponseDTO<Page<ToolCategoryDTO>> getAllToolCategories(Pageable pageable) {
        try {
            log.info("Fetching tool categories with pagination - page: {}, size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<ToolCategory> categoriesPage = toolCategoryRepository.findAll(pageable);

            if (categoriesPage.isEmpty()) {
                log.info("No tool categories found with the given pagination");
                return BaseResponseDTO.success("No tool categories found", categoriesPage.map(this::convertToDTO));
            }

            Page<ToolCategoryDTO> categoryDTOs = categoriesPage.map(this::convertToDTO);

            log.info("Successfully fetched {} tool categories out of {}",
                    categoryDTOs.getNumberOfElements(), categoryDTOs.getTotalElements());
            return BaseResponseDTO.success("Tool categories retrieved successfully", categoryDTOs);

        } catch (Exception e) {
            log.error("Error occurred while fetching paginated tool categories: {}", e.getMessage(), e);
            return BaseResponseDTO.error("Failed to retrieve tool categories: " + e.getMessage());
        }
    }


    public BaseResponseDTO<ToolCategoryDTO> createToolCategory(AddToolCategoryDTO addToolCategoryDTO) {
        try {
            log.info("Creating new tool category with name: {}", addToolCategoryDTO.getName());

            // Validate input
            if (addToolCategoryDTO.getName() == null || addToolCategoryDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Tool category name is required");
            }

            String categoryName = addToolCategoryDTO.getName().trim();

            // Check for duplicate name
            if (toolCategoryRepository.existsByNameIgnoreCase(categoryName)) {
                log.warn("Tool category with name '{}' already exists", categoryName);
                return BaseResponseDTO.error("Tool category with name '" + categoryName + "' already exists");
            }

            ToolCategory category = convertToEntity(addToolCategoryDTO);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());

            ToolCategory savedCategory = toolCategoryRepository.save(category);
            ToolCategoryDTO savedCategoryDTO = convertToDTO(savedCategory);

            log.info("Successfully created tool category with ID: {}", savedCategory.getId());
            return BaseResponseDTO.success("Tool category created successfully", savedCategoryDTO);

        } catch (Exception e) {
            log.error("Error occurred while creating tool category: {}", e.getMessage(), e);
            return BaseResponseDTO.error("Failed to create tool category: " + e.getMessage());
        }
    }


    public BaseResponseDTO<ToolCategoryDTO> updateToolCategory(Long id, AddToolCategoryDTO addToolCategoryDTO) {
        try {
            log.info("Updating tool category with ID: {}", id);

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
                log.warn("Tool category with ID {} not found for update", id);
                return BaseResponseDTO.error("Tool category not found with ID: " + id);
            }

            ToolCategory existingCategory = categoryOptional.get();

            // Check if name is being changed and if new name already exists
            if (!existingCategory.getName().equals(categoryName)) {
                Optional<ToolCategory> duplicateCategory = toolCategoryRepository
                        .findByNameAndIdNot(categoryName, id);
                if (duplicateCategory.isPresent()) {
                    log.warn("Another tool category with name '{}' already exists", categoryName);
                    return BaseResponseDTO.error("Another tool category with name '" + categoryName + "' already exists");
                }
            }

            // Update fields
            existingCategory.setName(categoryName);
            existingCategory.setDescription(addToolCategoryDTO.getDescription());
            existingCategory.setUpdatedAt(LocalDateTime.now());

            ToolCategory updatedCategory = toolCategoryRepository.save(existingCategory);
            ToolCategoryDTO updatedCategoryDTO = convertToDTO(updatedCategory);

            log.info("Successfully updated tool category with ID: {}", id);
            return BaseResponseDTO.success("Tool category updated successfully", updatedCategoryDTO);

        } catch (Exception e) {
            log.error("Error occurred while updating tool category with ID {}: {}", id, e.getMessage(), e);
            return BaseResponseDTO.error("Failed to update tool category: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Void> deleteToolCategory(Long id) {
        try {
            log.info("Deleting tool category with ID: {}", id);

            if (id == null || id <= 0) {
                return BaseResponseDTO.error("Invalid tool category ID");
            }

            Optional<ToolCategory> categoryOptional = toolCategoryRepository.findById(id);

            if (categoryOptional.isEmpty()) {
                log.warn("Tool category with ID {} not found for deletion", id);
                return BaseResponseDTO.error("Tool category not found with ID: " + id);
            }

            ToolCategory category = categoryOptional.get();

            // Check if category has associated tools
            if (category.getTools() != null && !category.getTools().isEmpty()) {
                log.warn("Cannot delete tool category with ID {} as it has {} associated tools",
                        id, category.getTools().size());
                return BaseResponseDTO.error("Cannot delete tool category as it has associated tools. Please remove the tools first.");
            }

            toolCategoryRepository.deleteById(id);
            log.info("Successfully deleted tool category with ID: {}", id);
            return BaseResponseDTO.success("Tool category deleted successfully", null);

        } catch (Exception e) {
            log.error("Error occurred while deleting tool category with ID {}: {}", id, e.getMessage(), e);
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