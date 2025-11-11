package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.BaseRequestDTO;
import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.CountResponseDTO;
import com.example.inventory_factory_management.DTO.ProductDTO;
import com.example.inventory_factory_management.Specifications.ProductSpecification;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.product;
import com.example.inventory_factory_management.entity.productCategory;
import com.example.inventory_factory_management.repository.productCategoryRepository;
import com.example.inventory_factory_management.repository.productRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class productService {

    @Autowired
    productRepository productRepository;

    @Autowired
    productCategoryRepository productCategoryRepository;

    @Autowired
    CloudinaryService cloudinaryService;


    public BaseResponseDTO<ProductDTO> createProduct(ProductDTO productDTO, MultipartFile imageFile) {
        try {
            if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Product name is required");
            }
            if (productDTO.getCategoryId() == null) {
                return BaseResponseDTO.error("Category ID is required");
            }
            if (productRepository.existsByName(productDTO.getName())) {
                return BaseResponseDTO.error("Product with name '" + productDTO.getName() + "' already exists");
            }

            productCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + productDTO.getCategoryId()));

            product newProduct = new product();
            newProduct.setName(productDTO.getName());
            newProduct.setProdDescription(productDTO.getProdDescription());
            newProduct.setPrice(productDTO.getPrice());
            newProduct.setRewardPts(productDTO.getRewardPts());
            newProduct.setCategory(category);

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                newProduct.setImage(imageUrl);
            } else if (productDTO.getImage() != null && !productDTO.getImage().trim().isEmpty()) {
                newProduct.setImage(productDTO.getImage());
            }

            newProduct.setStatus(account_status.ACTIVE);

            product savedProduct = productRepository.save(newProduct);
            return BaseResponseDTO.success("Product created successfully", convertToDTO(savedProduct));

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create product: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ProductDTO> updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile) {
        try {
            product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            // Handle image upload if new image provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                existingProduct.setImage(imageUrl);
            } else if (productDTO.getImage() != null && !productDTO.getImage().trim().isEmpty()) {
                // Keep existing image URL if no new image provided
                existingProduct.setImage(productDTO.getImage());
            }

            // Update other fields
            if (productDTO.getName() != null) existingProduct.setName(productDTO.getName());
            if (productDTO.getProdDescription() != null) existingProduct.setProdDescription(productDTO.getProdDescription());
            if (productDTO.getPrice() != null) existingProduct.setPrice(productDTO.getPrice());
            if (productDTO.getRewardPts() != null) existingProduct.setRewardPts(productDTO.getRewardPts());
            if (productDTO.getCategoryId() != null) {
                productCategory category = productCategoryRepository.findById(productDTO.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                existingProduct.setCategory(category);
            }

            existingProduct.setUpdatedAt(LocalDateTime.now());
            product updatedProduct = productRepository.save(existingProduct);

            return BaseResponseDTO.success("Product updated successfully", convertToDTO(updatedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update product: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Page<ProductDTO>> getAllProducts(BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<product> productPage = productRepository.findByStatus(account_status.ACTIVE, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Products retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve products: " + e.getMessage());
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

    public BaseResponseDTO<ProductDTO> getProductDetail(Long id) {
        try {
            product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(product));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get product details: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDTO<ProductDTO> deactivateProduct(Long id) {
        try {
            product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            product.setStatus(account_status.INACTIVE);
            product.setUpdatedAt(LocalDateTime.now());
            product updatedProduct = productRepository.save(product);

            return BaseResponseDTO.success("Product deactivated successfully", convertToDTO(updatedProduct));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to deactivate product: " + e.getMessage());
        }
    }


    private ProductDTO convertToDTO(product p) {
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



    // UPDATED: Get all products with filtering and searching
    public BaseResponseDTO<Page<ProductDTO>> getAllProducts(BaseRequestDTO request, String search, Long categoryId, String status) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

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
            Specification<product> spec = ProductSpecification.withFilters(search, categoryId, accountStatus);

            Page<product> productPage;
            if (spec != null) {
                productPage = productRepository.findAll(spec, pageable);
            } else {
                productPage = productRepository.findByStatus(account_status.ACTIVE, pageable);
            }

            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve products: " + e.getMessage());
        }
    }

    // NEW: Search products by name with pagination
    public BaseResponseDTO<Page<ProductDTO>> searchProductsByName(String search, BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Specification<product> spec = ProductSpecification.withFilters(search, null, account_status.ACTIVE);
            Page<product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products search completed successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search products: " + e.getMessage());
        }
    }

    // NEW: Get products by category
    public BaseResponseDTO<Page<ProductDTO>> getProductsByCategory(Long categoryId, BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Specification<product> spec = ProductSpecification.withFilters(null, categoryId, account_status.ACTIVE);
            Page<product> productPage = productRepository.findAll(spec, pageable);
            Page<ProductDTO> dtoPage = productPage.map(this::convertToDTO);
            return BaseResponseDTO.success("Products retrieved by category successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve products by category: " + e.getMessage());
        }
    }
}
