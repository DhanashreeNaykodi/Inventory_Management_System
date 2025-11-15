package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.ToolCategoryService;
import com.example.inventory_factory_management.service.ToolService;
import com.example.inventory_factory_management.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    @Autowired
    private ToolCategoryService toolCategoryService;

    @Autowired
    private ToolService toolService;


    @GetMapping("/tool-categories")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<BaseResponseDTO<Page<ToolCategoryDTO>>> getAllToolCategories(
            @ModelAttribute BaseRequestDTO request,
            @RequestParam(required = false) String search) {

        BaseResponseDTO<Page<ToolCategoryDTO>> response =
                toolCategoryService.getAllToolCategories(request, search);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/tool-categories/create")
    @PreAuthorize("hasAnyRole('OWNER')")
    public BaseResponseDTO<ToolCategoryDTO> createToolCategory(@RequestBody AddToolCategoryDTO addToolCategoryDTO) {
        BaseResponseDTO<ToolCategoryDTO> response = toolCategoryService.createToolCategory(addToolCategoryDTO);
        return response;
    }


    @PutMapping("/tool-categories/{id}")
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<ToolCategoryDTO>> updateToolCategory(
            @PathVariable Long id,
            @RequestBody AddToolCategoryDTO addToolCategoryDTO) {

        BaseResponseDTO<ToolCategoryDTO> response = toolCategoryService.updateToolCategory(id, addToolCategoryDTO);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }


    @DeleteMapping("/tool-categories/{id}")
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<Void>> deleteToolCategory(@PathVariable Long id) {
        BaseResponseDTO<Void> response = toolCategoryService.deleteToolCategory(id);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }





    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<ToolResponseDTO>> createTool(
            @ModelAttribute CreateToolDTO createToolDTO) {
        BaseResponseDTO<ToolResponseDTO> response = toolService.createTool(createToolDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<BaseResponseDTO<Page<ToolResponseDTO>>> getAllTools(
            @ModelAttribute BaseRequestDTO request,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) ToolType type,
            @RequestParam(required = false) Expensive isExpensive,
            @RequestParam(required = false) String search) {

        BaseResponseDTO<Page<ToolResponseDTO>> response =
                toolService.getAllTools(request, categoryId, status, type, isExpensive, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CHIEF_SUPERVISOR')")
    public ResponseEntity<BaseResponseDTO<ToolResponseDTO>> getToolById(@PathVariable Long id) {
        BaseResponseDTO<ToolResponseDTO> response = toolService.getToolById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<ToolResponseDTO>> updateTool(
            @PathVariable Long id,
            @ModelAttribute CreateToolDTO updateToolDTO) {
        BaseResponseDTO<ToolResponseDTO> response = toolService.updateTool(id, updateToolDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<String>> deleteTool(@PathVariable Long id) {
        BaseResponseDTO<String> response = toolService.deleteTool(id);
        return ResponseEntity.ok(response);
    }



//    private final ToolRequestService toolRequestService;
//
//    @PostMapping("/request")
//    @PreAuthorize("hasRole('FACTORY_MANAGER')")
//    public ResponseEntity<BaseResponseDTO<ToolRequestResponseDTO>> createToolRequest(
//            @RequestBody CreateToolRequestDTO requestDTO) {
//        BaseResponseDTO<ToolRequestResponseDTO> response = toolRequestService.createToolRequest(requestDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @GetMapping("/my-requests")
//    @PreAuthorize("hasRole('FACTORY_MANAGER')")
//    public ResponseEntity<BaseResponseDTO<List<ToolRequestResponseDTO>>> getMyToolRequests(
//            BaseRequestDTO request) {
//        BaseResponseDTO<List<ToolRequestResponseDTO>> response =
//                toolRequestService.getMyToolRequests(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/requests/pending")
//    @PreAuthorize("hasRole('CHIEF_OFFICER')")
//    public ResponseEntity<BaseResponseDTO<List<ToolRequestResponseDTO>>> getPendingRequests(
//            BaseRequestDTO request) {
//        BaseResponseDTO<List<ToolRequestResponseDTO>> response =
//                toolRequestService.getPendingRequests(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/requests/{id}/approve")
//    @PreAuthorize("hasRole('CHIEF_OFFICER')")
//    public ResponseEntity<BaseResponseDTO<ToolRequestResponseDTO>> approveToolRequest(
//            @PathVariable Long id) {
//        BaseResponseDTO<ToolRequestResponseDTO> response = toolRequestService.approveToolRequest(id);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/requests/{id}/receive")
//    @PreAuthorize("hasRole('FACTORY_MANAGER')")
//    public ResponseEntity<BaseResponseDTO<ToolRequestResponseDTO>> markAsReceived(
//            @PathVariable Long id) {
//        BaseResponseDTO<ToolRequestResponseDTO> response = toolRequestService.markAsReceived(id);
//        return ResponseEntity.ok(response);
//    }

}