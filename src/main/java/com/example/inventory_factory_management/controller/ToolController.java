package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.ToolCategoryService;
import com.example.inventory_factory_management.service.ToolRequestService;
import com.example.inventory_factory_management.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    @Autowired
    private ToolCategoryService toolCategoryService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private ToolRequestService toolRequestService;


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
    public BaseResponseDTO<ToolCategoryDTO> createToolCategory(@Valid @RequestBody AddToolCategoryDTO addToolCategoryDTO) {
        BaseResponseDTO<ToolCategoryDTO> response = toolCategoryService.createToolCategory(addToolCategoryDTO);
        return response;
    }


    @PutMapping("/tool-categories/{id}")
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<ToolCategoryDTO>> updateToolCategory(
            @PathVariable Long id,
            @Valid @RequestBody AddToolCategoryDTO addToolCategoryDTO) {

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
            @Valid @ModelAttribute CreateToolDTO createToolDTO) {
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
            @Valid @ModelAttribute CreateToolDTO updateToolDTO) {
        BaseResponseDTO<ToolResponseDTO> response = toolService.updateTool(id, updateToolDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<String>> deleteTool(@PathVariable Long id) {
        BaseResponseDTO<String> response = toolService.deleteTool(id);
        return ResponseEntity.ok(response);
    }


//  Manager adds tools into their factory
    @PostMapping("/stock/add")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BaseResponseDTO<String>> addToolsToFactoryStock(
            @Valid @RequestBody AssignToolToFactoryDTO requestDTO) {
         BaseResponseDTO<String> response = toolService.addToolsToFactoryStock(requestDTO);
         HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
         return ResponseEntity.status(status).body(response);
    }


    @GetMapping("/stock/my-factory")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BaseResponseDTO<Page<ToolStockResponseDTO>>> getMyFactoryTools(
            @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<ToolStockResponseDTO>> response = toolService.getMyFactoryTools(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stock/my-factory/storage-details/{toolId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BaseResponseDTO<ToolStorageDetailDTO>> getToolStorageDetails(@PathVariable Long toolId) {
        BaseResponseDTO<ToolStorageDetailDTO> response = toolService.getToolStorageDetails(toolId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/storage-locations/codes")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BaseResponseDTO<Page<String>>> getStorageLocationCodes(
            @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<String>> response = toolService.getStorageLocationCodes(request);
        return ResponseEntity.ok(response);
    }


    // Worker creates tool request
    @PostMapping("/requests/create")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<BaseResponseDTO<WorkerToolResponseDTO>> createToolRequest(
             @Valid @RequestBody CreateToolRequestDTO requestDTO) {
        BaseResponseDTO<WorkerToolResponseDTO> response = toolRequestService.createToolRequest(requestDTO);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    // Get pending requests for approval (Chief Supervisor & Manager)
    @GetMapping("/requests/pending")
    @PreAuthorize("hasAnyRole('CHIEF_SUPERVISOR', 'MANAGER', 'WORKER')")
    public ResponseEntity<BaseResponseDTO<Page<WorkerToolResponseDTO>>> getPendingRequests(
            @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<WorkerToolResponseDTO>> response = toolRequestService.getPendingRequests(request);
        return ResponseEntity.ok(response);
    }

}