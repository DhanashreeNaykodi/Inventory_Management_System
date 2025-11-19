package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.ProductService;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.validations.ValidImage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;


@RestController
@RequestMapping("/owner")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Category-wise product counts with pagination
    @PostMapping("/category-products")
    public ResponseEntity<BaseResponseDTO<Page<CategoryProductCountDTO>>> getCategoryWiseProductCounts(
            @RequestBody BaseRequestDTO request) {
        BaseResponseDTO<Page<CategoryProductCountDTO>> response = productService.getCategoryWiseProductCounts(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/createProduct")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        BaseResponseDTO<ProductDTO> response = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PreAuthorize("hasRole('OWNER')")
    @PostMapping(value = "/uploadImage/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponseDTO<?>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("imageFile") MultipartFile imageFile) {

        BaseResponseDTO<?> response = productService.uploadProductImage(productId, imageFile);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CENTRAL_OFFICER', 'DISTRIBUTOR')")
    @GetMapping("/{id}/detail")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> getProductDetail(@PathVariable Long id) {
        BaseResponseDTO<ProductDTO> response = productService.getProductDetail(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> deactivateProduct(@PathVariable Long id) {
        BaseResponseDTO<ProductDTO> response = productService.deactivateProduct(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        BaseResponseDTO<ProductDTO> response = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/productCount")
    public ResponseEntity<BaseResponseDTO<CountResponseDTO>> getCount() {
        BaseResponseDTO<CountResponseDTO> response = productService.getProductsCount();
        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER','CENTRAL_OFFICER', 'DISTRIBUTOR')")
    @GetMapping("/products")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> getAllProducts(
            @Valid @ModelAttribute BaseRequestDTO request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.getAllProducts(request, search, categoryId, status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CENTRAL_OFFICER', 'DISTRIBUTOR')")
    @GetMapping("/products/search")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> searchProducts(
            @RequestParam String search,
            @Valid @ModelAttribute BaseRequestDTO request) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.searchProductsByName(search, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CENTRAL_OFFICER', 'DISTRIBUTOR')")
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @Valid @ModelAttribute BaseRequestDTO request) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.getProductsByCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }
}