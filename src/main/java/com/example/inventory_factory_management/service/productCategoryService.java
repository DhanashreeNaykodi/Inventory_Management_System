package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.CategoryDTO;
import com.example.inventory_factory_management.Specifications.ProductCategorySpecifications;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.productCategory;
import com.example.inventory_factory_management.repository.productCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class productCategoryService {

    @Autowired
    private productCategoryRepository productCategoryRepository;

    // CREATE CATEGORY
    public BaseResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO) {
        try {
            if (categoryDTO.getCategoryName() == null || categoryDTO.getCategoryName().trim().isEmpty()) {
                return BaseResponseDTO.error("Category name is required");
            }

            if (productCategoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
                return BaseResponseDTO.error("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            productCategory category = new productCategory();
            category.setCategoryName(categoryDTO.getCategoryName());
            category.setDescription(categoryDTO.getDescription());

            // Save category
            productCategory savedCategory = productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category created successfully", convertToDTO(savedCategory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create category: " + e.getMessage());
        }
    }

    // GET ALL CATEGORIES
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        try {
            Page<productCategory> categoryPage = productCategoryRepository.findAll(pageable);
            Page<CategoryDTO> dtoPage = categoryPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Categories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve categories: " + e.getMessage());
        }
    }

    // GET ALL CATEGORIES (without pagination - for dropdowns)
    public BaseResponseDTO<List<CategoryDTO>> getAllCategoriesList() {
        try {
            List<productCategory> categories = productCategoryRepository.findAll();
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return BaseResponseDTO.success("Categories retrieved successfully", categoryDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve categories: " + e.getMessage());
        }
    }

    // GET CATEGORY BY ID
    public BaseResponseDTO<CategoryDTO> getCategoryById(Long id) {
        try {
            productCategory category = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(category));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get category: " + e.getMessage());
        }
    }


    // NEW: Get category by exact name
    public BaseResponseDTO<CategoryDTO> getCategoryByName(String categoryName) {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return BaseResponseDTO.error("Category name is required");
            }

            // Find by exact name (case-sensitive)
            Optional<productCategory> category = productCategoryRepository.findByCategoryName(categoryName);

            if (category.isEmpty()) {
                return BaseResponseDTO.error("Category not found with name: " + categoryName);
            }

            return BaseResponseDTO.success("Category retrieved successfully", convertToDTO(category.get()));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get category: " + e.getMessage());
        }
    }


    // UPDATE CATEGORY
    public BaseResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO) {
        try {
            productCategory existingCategory = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

            // Check if new name conflicts with other categories
            if (categoryDTO.getCategoryName() != null &&
                    !existingCategory.getCategoryName().equals(categoryDTO.getCategoryName()) &&
                    productCategoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
                return BaseResponseDTO.error("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            // Update fields
            if (categoryDTO.getCategoryName() != null) {
                existingCategory.setCategoryName(categoryDTO.getCategoryName());
            }
            if (categoryDTO.getDescription() != null) {
                existingCategory.setDescription(categoryDTO.getDescription());
            }

            productCategory updatedCategory = productCategoryRepository.save(existingCategory);
            return BaseResponseDTO.success("Category updated successfully", convertToDTO(updatedCategory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update category: " + e.getMessage());
        }
    }

    // DELETE CATEGORY
    @Transactional
    public BaseResponseDTO<String> deleteCategory(Long id) {
        try {
            productCategory category = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

            // Check if category has products
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                return BaseResponseDTO.error("Cannot delete category with existing products");
            }

//            productCategoryRepository.delete(category);
            category.setStatus(account_status.INACTIVE);
            productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category deleted successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete category: " + e.getMessage());
        }
    }


    // UPDATED: Get all categories with filtering and searching
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable, String search, String status) {
        try {
            // Convert status string to enum
            account_status accountStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    accountStatus = account_status.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // If invalid status provided, ignore the filter
                }
            }

            // Build specification with filters
            Specification<productCategory> spec = ProductCategorySpecifications.withFilters(search, accountStatus);

            Page<productCategory> categoryPage;
            if (spec != null) {
                categoryPage = productCategoryRepository.findAll(spec, pageable);
            } else {
                categoryPage = productCategoryRepository.findAll(pageable);
            }

            Page<CategoryDTO> dtoPage = categoryPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Categories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve categories: " + e.getMessage());
        }
    }

    // NEW: Search categories by name
    public BaseResponseDTO<List<CategoryDTO>> searchCategoriesByName(String search) {
        try {
            Specification<productCategory> spec = ProductCategorySpecifications.withFilters(search, account_status.ACTIVE);
            List<productCategory> categories = productCategoryRepository.findAll(spec);
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return BaseResponseDTO.success("Categories search completed successfully", categoryDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search categories: " + e.getMessage());
        }
    }


    // HELPER METHOD
    private CategoryDTO convertToDTO(productCategory category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDescription(category.getDescription());

        // Count products in this category
        if (category.getProducts() != null) {
            dto.setProductCount(category.getProducts().size());
        } else {
            dto.setProductCount(0);
        }

        return dto;
    }
}