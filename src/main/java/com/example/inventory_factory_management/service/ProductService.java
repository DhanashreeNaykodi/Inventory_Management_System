package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.specifications.ProductCategorySpecifications;
import com.example.inventory_factory_management.specifications.ProductSpecifications;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Product;
import com.example.inventory_factory_management.entity.ProductCategory;
import com.example.inventory_factory_management.repository.ProductCategoryRepository;
import com.example.inventory_factory_management.repository.ProductRepository;
import com.example.inventory_factory_management.utils.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// CRUD & operations - product and product category

@Service
@Transactional
public class ProductService {


    private ProductRepository productRepository;
    private ProductCategoryRepository productCategoryRepository;
    private CloudinaryService cloudinaryService;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.cloudinaryService = cloudinaryService;

    }


    /// / PRODUCT
    public BaseResponseDTO<ProductDTO> createProduct(ProductDTO productDTO) {
//        try {

        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            return BaseResponseDTO.error("Product name is required");
        }
        if (productDTO.getCategoryId() == null) {
            return BaseResponseDTO.error("Category ID is required");
        }
        if (productRepository.existsByNameIgnoreCase(productDTO.getName())) {
            return BaseResponseDTO.error("Product with name '" + productDTO.getName() + "' already exists");
        }

        ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));

        Product newProduct = new Product();
        newProduct.setName(productDTO.getName());
        newProduct.setProdDescription(productDTO.getProdDescription());
        newProduct.setPrice(productDTO.getPrice());
        newProduct.setRewardPts(productDTO.getRewardPts());
        newProduct.setCategory(category);

        // Set default image if no image provided
        if (productDTO.getImage() != null && !productDTO.getImage().trim().isEmpty()) {
            newProduct.setImage(productDTO.getImage()); // Use provided image URL
        } else {
            newProduct.setImage("src/main/resources/static/images/user-profile-icon.jpg"); // Default image
        }
        newProduct.setStatus(AccountStatus.ACTIVE);
//            Product savedProduct = productRepository.save(newProduct);
//            return BaseResponseDTO.success("Product created successfully", convertToDTO(savedProduct));
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to create product: " + e.getMessage());
//        }
        try {
            Product savedProduct = productRepository.save(newProduct);
            return BaseResponseDTO.success("Product created successfully", convertToDTO(savedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create product: " + e.getMessage());
        }
    }


    public BaseResponseDTO<?> uploadProductImage(Long productId, MultipartFile imageFile) {
        try {
            Product existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            if (imageFile == null || imageFile.isEmpty()) {
                return BaseResponseDTO.error("Image file is required");
            }
            String imageUrl = cloudinaryService.uploadFile(imageFile);
            existingProduct.setImage(imageUrl);
            existingProduct.setUpdatedAt(LocalDateTime.now());

            productRepository.save(existingProduct);
            return BaseResponseDTO.success("Product image uploaded successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to upload product image: " + e.getMessage());
        }
    }


    public BaseResponseDTO<ProductDTO> getProductDetail(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(product));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get product details: " + e.getMessage());
        }
    }


    @Transactional
    public BaseResponseDTO<ProductDTO> deactivateProduct(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            product.setStatus(AccountStatus.INACTIVE);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            Product updatedProduct = productRepository.save(product);

            return BaseResponseDTO.success("Product deactivated successfully", convertToDTO(updatedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to deactivate product: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {
        try {
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            if (productDTO.getImage() != null) {
                existingProduct.setImage(productDTO.getImage());
            }

            if (productDTO.getName() != null && !productRepository.existsByNameIgnoreCase(productDTO.getName())) {
                existingProduct.setName(productDTO.getName());
            }
            else if(productRepository.existsByNameIgnoreCase(productDTO.getName())) {
                throw new RuntimeException("Product name already exists");
            }
            if (productDTO.getProdDescription() != null) existingProduct.setProdDescription(productDTO.getProdDescription());
            if (productDTO.getPrice() != null) existingProduct.setPrice(productDTO.getPrice());
            if (productDTO.getRewardPts() != null) existingProduct.setRewardPts(productDTO.getRewardPts());
            if (productDTO.getCategoryId() != null) {
                ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                existingProduct.setCategory(category);
            }

            existingProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(existingProduct);

            return BaseResponseDTO.success("Product updated successfully", convertToDTO(updatedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update product: " + e.getMessage());
        }
    }

    private ProductDTO convertToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setImage(p.getImage()); // This will include the URL
        dto.setProdDescription(p.getProdDescription());
        dto.setPrice(p.getPrice());
        dto.setRewardPts(p.getRewardPts());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getCategory() != null) {
            dto.setCategoryName(p.getCategory().getCategoryName());
            dto.setCategoryId(p.getCategory().getId());
        }
        return dto;
    }

    // Get all products with filtering and searching
    public BaseResponseDTO<Page<ProductDTO>> getAllProducts(BaseRequestDTO request, String search, Long categoryId, String status) {
        try {

            if (categoryId != null) {
                boolean categoryExists = productCategoryRepository.existsById(categoryId);
                if (!categoryExists) {
                    throw new IllegalArgumentException("Incorrect category ID: " + categoryId);
                }
            }

            Pageable pageable = PaginationUtil.toPageable(request);
            AccountStatus accountStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    accountStatus = AccountStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            Specification<Product> spec = ProductSpecifications.withFilters(search, categoryId, accountStatus);
            Page<Product> productPage;
            if (spec != null) {
                productPage = productRepository.findAll(spec, pageable);
            } else {
                productPage = productRepository.findByStatus(AccountStatus.ACTIVE, pageable);
            }

            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve products: " + e.getMessage());
        }
    }


    /// / PRODUCT CATEGORY

    public BaseResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO) {
        try {
            if (categoryDTO.getCategoryName() == null || categoryDTO.getCategoryName().trim().isEmpty()) {
                return BaseResponseDTO.error("Category name is required");
            }

            if (productCategoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
                return BaseResponseDTO.error("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            ProductCategory category = new ProductCategory();
            category.setCategoryName(categoryDTO.getCategoryName().trim());
            category.setDescription(categoryDTO.getDescription());

            // Save category
            ProductCategory savedCategory = productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category created successfully", convertToDTO(savedCategory));
        }
        catch (Exception e) {
            return BaseResponseDTO.error("Failed to create category: " + e.getMessage());
        }
    }

    // GET ALL CATEGORIES
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        try {
            Page<ProductCategory> categoryPage = productCategoryRepository.findAll(pageable);
            Page<CategoryDTO> dtoPage = categoryPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Categories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve categories: " + e.getMessage());
        }
    }


    public BaseResponseDTO<CategoryDTO> getCategoryById(Long id) {
        try {
            ProductCategory category = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(category));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get category: " + e.getMessage());
        }
    }

    // Get category by name
    public BaseResponseDTO<CategoryDTO> getCategoryByName(String categoryName) {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return BaseResponseDTO.error("Category name is required");
            }

            Optional<ProductCategory> category = productCategoryRepository.findByCategoryNameContainingIgnoreCase(categoryName);

            if (category.isEmpty()) {
                return BaseResponseDTO.error("Category not found with name: " + categoryName);
            }

            return BaseResponseDTO.success("Category retrieved successfully", convertToDTO(category.get()));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get category: " + e.getMessage());
        }
    }

    public BaseResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO) {
        try {
            ProductCategory existingCategory = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

            //if new name conflicts with other categories
            if (categoryDTO.getCategoryName() != null &&
                    !existingCategory.getCategoryName().equalsIgnoreCase(categoryDTO.getCategoryName()) &&
                    productCategoryRepository.existsByCategoryNameIgnoreCase(categoryDTO.getCategoryName())) {
                return BaseResponseDTO.error("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            if (categoryDTO.getCategoryName() != null) {
                existingCategory.setCategoryName(categoryDTO.getCategoryName());
            }
            if (categoryDTO.getDescription() != null) {
                existingCategory.setDescription(categoryDTO.getDescription());
            }

            ProductCategory updatedCategory = productCategoryRepository.save(existingCategory);
            return BaseResponseDTO.success("Category updated successfully", convertToDTO(updatedCategory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update category: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDTO<String> deleteCategory(Long id) {
        try {
            ProductCategory category = productCategoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

            // Check if category has products
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                return BaseResponseDTO.error("Cannot delete category with existing products");
            }

//            productCategoryRepository.delete(category);
            category.setStatus(AccountStatus.INACTIVE);
            productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category deleted successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete category: " + e.getMessage());
        }
    }

    // Get all categories with filtering and searching
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable, String search, String status) {
        try {

            AccountStatus accountStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    accountStatus = AccountStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            Specification<ProductCategory> spec = ProductCategorySpecifications.withFilters(search, accountStatus);

            Page<ProductCategory> categoryPage;
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



    // Search categories by name
    public BaseResponseDTO<List<CategoryDTO>> searchCategoriesByName(String search) {
        try {
            Specification<ProductCategory> spec = ProductCategorySpecifications.withFilters(search, AccountStatus.ACTIVE);
            List<ProductCategory> categories = productCategoryRepository.findAll(spec);
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return BaseResponseDTO.success("Categories search completed successfully", categoryDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search categories: " + e.getMessage());
        }
    }

    // GET ALL CATEGORIES
    public BaseResponseDTO<List<CategoryDTO>> getAllCategoriesList() {
        try {
            List<ProductCategory> categories = productCategoryRepository.findAll();
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return BaseResponseDTO.success("Categories retrieved successfully", categoryDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve categories: " + e.getMessage());
        }
    }

    // HELPER METHOD
    private CategoryDTO convertToDTO(ProductCategory category) {
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



    // Search products by name with pagination
    public BaseResponseDTO<Page<ProductDTO>> searchProductsByName(String search, BaseRequestDTO request) {
        try {
//            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Pageable pageable = PaginationUtil.toPageable(request);

            Specification<Product> spec = ProductSpecifications.withFilters(search, null, AccountStatus.ACTIVE);
            Page<Product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products search completed successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search products: " + e.getMessage());
        }
    }

    // Get products by category
    public BaseResponseDTO<Page<ProductDTO>> getProductsByCategory(Long categoryId, BaseRequestDTO request) {
        try {
//            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Pageable pageable = PaginationUtil.toPageable(request);

            Specification<Product> spec = ProductSpecifications.withFilters(null, categoryId, AccountStatus.ACTIVE);
            Page<Product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products retrieved by category successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve products by category: " + e.getMessage());
        }
    }






    public BaseResponseDTO<Page<CategoryProductCountDTO>> getCategoryWiseProductCounts(BaseRequestDTO request) {
        try {
            Pageable pageable = PaginationUtil.toPageable(request, "categoryName");

            Page<Object[]> results = productCategoryRepository.getCategoryWiseProductCounts(pageable);

            // Convert Page<Object[]> to Page<CategoryProductCountDTO>
            Page<CategoryProductCountDTO> resultPage = results.map(result ->
                    new CategoryProductCountDTO(
                            (Long) result[0],    // categoryId
                            (String) result[1],  // categoryName
                            (Long) result[2]     // productCount
                    )
            );

            return BaseResponseDTO.success("Category product counts retrieved successfully", resultPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Error retrieving category product counts: " + e.getMessage());
        }
    }

    public BaseResponseDTO<CountResponseDTO> getProductsCount() {
        try {
            long count = productRepository.count();
            return BaseResponseDTO.success(CountResponseDTO.of(count, "products"));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get product count: " + e.getMessage());
        }
    }

}
