package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.service.productService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/owner")
public class productController {

    @Autowired
    private productService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponseDTO<ProductDTO> createProduct(
            @RequestPart("productData") @Valid ProductRequestDTO productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        // Convert to ProductDTO
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(productRequest.getName());
        productDTO.setProdDescription(productRequest.getProdDescription());
        productDTO.setPrice(productRequest.getPrice());
        productDTO.setRewardPts(productRequest.getRewardPts());
        productDTO.setCategoryId(productRequest.getCategoryId());

        return productService.createProduct(productDTO, imageFile);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponseDTO<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("productData") @Valid ProductRequestDTO productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        // Convert to ProductDTO
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(productRequest.getName());
        productDTO.setProdDescription(productRequest.getProdDescription());
        productDTO.setPrice(productRequest.getPrice());
        productDTO.setRewardPts(productRequest.getRewardPts());
        productDTO.setCategoryId(productRequest.getCategoryId());

        return productService.updateProduct(id, productDTO, imageFile);
    }


    @GetMapping("/{id}/detail")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> getProductDetail(@PathVariable Long id) {
        BaseResponseDTO<ProductDTO> response = productService.getProductDetail(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BaseResponseDTO<ProductDTO>> deactivateProduct(@PathVariable Long id) {
        // This method remains in your service
        BaseResponseDTO<ProductDTO> response = productService.deactivateProduct(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/productCount")
    public ResponseEntity<BaseResponseDTO<CountResponseDTO>> getCount() {
        BaseResponseDTO<CountResponseDTO> response = productService.getProductsCount();
        return ResponseEntity.ok(response);
    }


    // UPDATED: Get all products with filtering
    @GetMapping("/products")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> getAllProducts(
            @Valid BaseRequestDTO request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.getAllProducts(request, search, categoryId, status);
        return ResponseEntity.ok(response);
    }

    // NEW: Search products by name
    @GetMapping("/products/search")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> searchProducts(
            @RequestParam String search,
            @Valid BaseRequestDTO request) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.searchProductsByName(search, request);
        return ResponseEntity.ok(response);
    }

    // NEW: Get products by category
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<BaseResponseDTO<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @Valid BaseRequestDTO request) {

        BaseResponseDTO<Page<ProductDTO>> response = productService.getProductsByCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }
}