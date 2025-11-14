package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.BaseRequestDTO;
import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.CategoryDTO;
import com.example.inventory_factory_management.service.ProductService;
import com.example.inventory_factory_management.utils.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product/categories")
public class ProductCategoryController {

    @Autowired
    private ProductService productService;

    // CREATE CATEGORY
    @PostMapping("/createCategory")
    public ResponseEntity<BaseResponseDTO<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        BaseResponseDTO<CategoryDTO> response = productService.createCategory(categoryDTO);
        return ResponseEntity.ok(response);
    }


    // GET ALL CATEGORIES (without pagination - for dropdowns)
    @GetMapping("/all")
    public ResponseEntity<BaseResponseDTO<List<CategoryDTO>>> getAllCategoriesList() {
        BaseResponseDTO<List<CategoryDTO>> response = productService.getAllCategoriesList();
        return ResponseEntity.ok(response);
    }

    // GET CATEGORY BY ID
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        BaseResponseDTO<CategoryDTO> response = productService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }


    // NEW: Get category by exact name
    @GetMapping("/name/{categoryName}")
    public ResponseEntity<BaseResponseDTO<CategoryDTO>> getCategoryByName(@PathVariable String categoryName) {
        BaseResponseDTO<CategoryDTO> response = productService.getCategoryByName(categoryName);
        return ResponseEntity.ok(response);
    }

    // UPDATE CATEGORY
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        BaseResponseDTO<CategoryDTO> response = productService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(response);
    }

    // DELETE CATEGORY
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<String>> deleteCategory(@PathVariable Long id) {
        BaseResponseDTO<String> response = productService.deleteCategory(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/")
    public ResponseEntity<BaseResponseDTO<Page<CategoryDTO>>> getAllCategories(
            @Valid BaseRequestDTO request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

//        Pageable pageable = PageRequest.of(
//                request.getPage(),
//                request.getSize(),
//                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
//        );
        Pageable pageable = PaginationUtil.toPageable(request);

        BaseResponseDTO<Page<CategoryDTO>> response = productService.getAllCategories(pageable, search, status);
        return ResponseEntity.ok(response);
    }

    // NEW: Search categories by name
    @GetMapping("/search")
    public ResponseEntity<BaseResponseDTO<List<CategoryDTO>>> searchCategories(
            @RequestParam String search) {

        BaseResponseDTO<List<CategoryDTO>> response = productService.searchCategoriesByName(search);
        return ResponseEntity.ok(response);
    }
}