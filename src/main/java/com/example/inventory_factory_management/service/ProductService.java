package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.exceptions.OperationNotPermittedException;
import com.example.inventory_factory_management.exceptions.ResourceAlreadyExistsException;
import com.example.inventory_factory_management.exceptions.ResourceNotFoundException;
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

        if (productRepository.existsByNameIgnoreCase(productDTO.getName())) {
            throw new ResourceAlreadyExistsException("Product with name '" + productDTO.getName() + "' already exists");
        }

        ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Product newProduct = new Product();
        newProduct.setName(productDTO.getName());
        newProduct.setProdDescription(productDTO.getProdDescription());
        newProduct.setPrice(productDTO.getPrice());
        newProduct.setRewardPts(productDTO.getRewardPts());
        newProduct.setCategory(category);

        // Set default image if no image provided
        if (productDTO.getImage() != null && !productDTO.getImage().trim().isEmpty()) {
            newProduct.setImage(productDTO.getImage());
        } else {
            newProduct.setImage("src/main/resources/static/images/user-profile-icon.jpg"); // Default image
        }
        newProduct.setStatus(AccountStatus.ACTIVE);

        try {
            Product savedProduct = productRepository.save(newProduct);
            return BaseResponseDTO.success("Product created successfully", convertToDTO(savedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create product: " + e.getMessage());
        }
    }


    public BaseResponseDTO<?> uploadProductImage(Long productId, MultipartFile imageFile) {
            Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("Image file is required");
            }
            String imageUrl = cloudinaryService.uploadFile(imageFile);
            existingProduct.setImage(imageUrl);
            existingProduct.setUpdatedAt(LocalDateTime.now());

            productRepository.save(existingProduct);
            return BaseResponseDTO.success("Product image uploaded successfully");

    }


    public BaseResponseDTO<ProductDTO> getProductDetail(Long id) {
            Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(product));
    }


    @Transactional
    public BaseResponseDTO<ProductDTO> deactivateProduct(Long id) {
            Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            product.setStatus(AccountStatus.INACTIVE);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            Product updatedProduct = productRepository.save(product);

            return BaseResponseDTO.success("Product deactivated successfully", convertToDTO(updatedProduct));
    }


    public BaseResponseDTO<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {
            Product existingProduct = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            if (productDTO.getName() != null && !productDTO.getName().equals(existingProduct.getName())) {
                boolean nameExists = productRepository.existsByNameIgnoreCaseAndIdNot(productDTO.getName(), id);
                if (nameExists) {
                    throw new OperationNotPermittedException("Product name '" + productDTO.getName() + "' already exists");
                }
                existingProduct.setName(productDTO.getName());
            }

            if (productDTO.getProdDescription() != null) {
                existingProduct.setProdDescription(productDTO.getProdDescription());
            }
            if (productDTO.getPrice() != null) {
                existingProduct.setPrice(productDTO.getPrice());
            }
            if (productDTO.getRewardPts() != null){
                existingProduct.setRewardPts(productDTO.getRewardPts());
            }
            if (productDTO.getCategoryId() != null) {
                ProductCategory category = productCategoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                existingProduct.setCategory(category);
            }

            existingProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(existingProduct);

            return BaseResponseDTO.success("Product updated successfully", convertToDTO(updatedProduct));
    }

    private ProductDTO convertToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setImage(p.getImage());
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

    }


    /// / PRODUCT CATEGORY

    public BaseResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO) {

            if (productCategoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
                throw new ResourceAlreadyExistsException("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            ProductCategory category = new ProductCategory();
            category.setCategoryName(categoryDTO.getCategoryName().trim());
            category.setDescription(categoryDTO.getDescription());

            // Save category
            ProductCategory savedCategory = productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category created successfully", convertToDTO(savedCategory));

    }

    // GET ALL CATEGORIES
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable) {

            Page<ProductCategory> categoryPage = productCategoryRepository.findAll(pageable);
            Page<CategoryDTO> dtoPage = categoryPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Categories retrieved successfully", dtoPage);
    }

    public BaseResponseDTO<CategoryDTO> getCategoryById(Long id) {
            ProductCategory category = productCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(category));

    }

    // Get category by name
    public BaseResponseDTO<CategoryDTO> getCategoryByName(String categoryName) {

            Optional<ProductCategory> category = productCategoryRepository.findByCategoryNameContainingIgnoreCase(categoryName);
            if (category.isEmpty()) {
                throw new ResourceNotFoundException("Category not found with name: " + categoryName);
            }

            return BaseResponseDTO.success("Category retrieved successfully", convertToDTO(category.get()));

    }

    public BaseResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO) {

            ProductCategory existingCategory = productCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

            if (categoryDTO.getCategoryName() != null &&
                    !existingCategory.getCategoryName().equalsIgnoreCase(categoryDTO.getCategoryName()) &&
                    productCategoryRepository.existsByCategoryNameIgnoreCase(categoryDTO.getCategoryName())) {
                throw new ResourceAlreadyExistsException("Category with name '" + categoryDTO.getCategoryName() + "' already exists");
            }

            if (categoryDTO.getCategoryName() != null) {
                existingCategory.setCategoryName(categoryDTO.getCategoryName());
            }
            if (categoryDTO.getDescription() != null) {
                existingCategory.setDescription(categoryDTO.getDescription());
            }

            ProductCategory updatedCategory = productCategoryRepository.save(existingCategory);
            return BaseResponseDTO.success("Category updated successfully", convertToDTO(updatedCategory));

    }

    @Transactional
    public BaseResponseDTO<String> deleteCategory(Long id) {

            ProductCategory category = productCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                throw new OperationNotPermittedException("Cannot delete category with existing products");
            }
            category.setStatus(AccountStatus.INACTIVE);
            productCategoryRepository.save(category);
            return BaseResponseDTO.success("Category deleted successfully");

    }

    // Get all categories with filtering and searching
    public BaseResponseDTO<Page<CategoryDTO>> getAllCategories(Pageable pageable, String search, String status) {

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

    }



    // Search categories by name
    public BaseResponseDTO<List<CategoryDTO>> searchCategoriesByName(String search) {
            Specification<ProductCategory> spec = ProductCategorySpecifications.withFilters(search, AccountStatus.ACTIVE);
            List<ProductCategory> categories = productCategoryRepository.findAll(spec);
            List<CategoryDTO> categoryDTOs = categories.stream().map(this::convertToDTO).collect(Collectors.toList());
            return BaseResponseDTO.success("Categories search completed successfully", categoryDTOs);

    }

    // GET ALL CATEGORIES
    public BaseResponseDTO<List<CategoryDTO>> getAllCategoriesList() {
            List<ProductCategory> categories = productCategoryRepository.findAll();
            List<CategoryDTO> categoryDTOs = categories.stream().map(this::convertToDTO).collect(Collectors.toList());
            return BaseResponseDTO.success("Categories retrieved successfully", categoryDTOs);
    }

    // HELPER METHOD
    private CategoryDTO convertToDTO(ProductCategory category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDescription(category.getDescription());

        if (category.getProducts() != null) {
            dto.setProductCount(category.getProducts().size());
        } else {
            dto.setProductCount(0);
        }

        return dto;
    }



    // Search products by name with pagination
    public BaseResponseDTO<Page<ProductDTO>> searchProductsByName(String search, BaseRequestDTO request) {

        Pageable pageable = PaginationUtil.toPageable(request);

            Specification<Product> spec = ProductSpecifications.withFilters(search, null, AccountStatus.ACTIVE);
            Page<Product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products search completed successfully", dtoPage);

    }

    // Get products by category
    public BaseResponseDTO<Page<ProductDTO>> getProductsByCategory(Long categoryId, BaseRequestDTO request) {

            Pageable pageable = PaginationUtil.toPageable(request);

            Specification<Product> spec = ProductSpecifications.withFilters(null, categoryId, AccountStatus.ACTIVE);
            Page<Product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products retrieved by category successfully", dtoPage);

    }


    public BaseResponseDTO<Page<CategoryProductCountDTO>> getCategoryWiseProductCounts(BaseRequestDTO request) {

            Pageable pageable = PaginationUtil.toPageable(request, "categoryName");

            Page<Object[]> results = productCategoryRepository.getCategoryWiseProductCounts(pageable);

            Page<CategoryProductCountDTO> resultPage = results.map(result ->
                    new CategoryProductCountDTO(
                            (Long) result[0],    // categoryId
                            (String) result[1],  // categoryName
                            (Long) result[2]     // productCount
                    )
            );

            return BaseResponseDTO.success("Category product counts retrieved successfully", resultPage);

    }

    public BaseResponseDTO<CountResponseDTO> getProductsCount() {

            long count = productRepository.count();
            return BaseResponseDTO.success(CountResponseDTO.of(count, "products"));

    }

}
