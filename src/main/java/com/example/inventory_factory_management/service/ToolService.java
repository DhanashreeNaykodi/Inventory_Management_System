package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.specifications.ToolSpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ToolService {

    @Autowired
    private final ToolRepository toolRepository;
    @Autowired
    private final ToolCategoryRepository toolCategoryRepository;
    @Autowired
    private CloudinaryService cloudinaryService;



    public BaseResponseDTO<ToolResponseDTO> createTool(CreateToolDTO createToolDTO) {
        try {
            // Check if tool name already exists
            if (toolRepository.existsByNameIgnoreCase(createToolDTO.getName())) {
                return BaseResponseDTO.error("Tool with this name already exists");
            }

            // Validate category
            ToolCategory category = toolCategoryRepository.findById(createToolDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));


            // Create tool entity
            Tool tool = new Tool();
            tool.setName(createToolDTO.getName());
            tool.setCategory(category);
            tool.setType(createToolDTO.getType());
            tool.setIsExpensive(createToolDTO.getIsExpensive());
            tool.setThreshold(createToolDTO.getThreshold());

            // Handle image upload
            String imageUrl = cloudinaryService.uploadFile(createToolDTO.getImage());
            tool.setImageUrl(imageUrl);
            tool.setUpdatedAt(LocalDateTime.now());
            tool.setStatus(AccountStatus.ACTIVE);

            Tool savedTool = toolRepository.save(tool);

            return BaseResponseDTO.success("Tool created successfully", convertToDTO(savedTool));

        } catch (Exception e) {
            return BaseResponseDTO.error("Error creating tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<ToolResponseDTO>> getAllTools(
            BaseRequestDTO request,
            Long categoryId,
            AccountStatus status,
            ToolType type,
            Expensive isExpensive,
            String search) {

        try {
            // Build specification with filters
            Specification<Tool> spec = ToolSpecifications.withFilters(
                    categoryId, status, type, isExpensive, search
            );

            // Apply pagination and sorting
            Pageable pageable = PaginationUtil.toPageable(request);

            // Execute query with specification and return Page directly
            Page<Tool> toolPage = toolRepository.findAll(spec, pageable);

            // Convert to DTO page
            Page<ToolResponseDTO> toolDTOsPage = toolPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Tools retrieved successfully", toolDTOsPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Error retrieving tools: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ToolResponseDTO> getToolById(Long id) {
        try {
            Tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
            return BaseResponseDTO.success("Tool retrieved successfully", convertToDTO(tool));
        } catch (Exception e) {
            return BaseResponseDTO.error("Error retrieving tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ToolResponseDTO> updateTool(Long id, CreateToolDTO updateToolDTO) {
        try {
            Tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            // Check name uniqueness if changed
            if (!tool.getName().equals(updateToolDTO.getName()) &&
                    toolRepository.existsByNameIgnoreCase(updateToolDTO.getName())) {
                return BaseResponseDTO.error("Tool with this name already exists");
            }

            // Update category if changed
            if (!tool.getCategory().getId().equals(updateToolDTO.getCategoryId())) {
                ToolCategory category = toolCategoryRepository.findById(updateToolDTO.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                tool.setCategory(category);
            }

            // Handle image update
            if (updateToolDTO.getImage() != null && !updateToolDTO.getImage().isEmpty()) {
                // Handle image upload
                String imageUrl = cloudinaryService.uploadFile(updateToolDTO.getImage());

                // Update tool with new image URL
                tool.setImageUrl(imageUrl);
            }

            // Update other fields
            tool.setName(updateToolDTO.getName());
            tool.setType(updateToolDTO.getType());
            tool.setIsExpensive(updateToolDTO.getIsExpensive());
            tool.setThreshold(updateToolDTO.getThreshold());
            tool.setUpdatedAt(LocalDateTime.now());
            Tool updatedTool = toolRepository.save(tool);

            return BaseResponseDTO.success("Tool updated successfully", convertToDTO(updatedTool));

        } catch (Exception e) {
            return BaseResponseDTO.error("Error updating tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<String> deleteTool(Long id) {
        try {
            Tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
//            tool.setStatus(AccountStatus.INACTIVE);
            toolRepository.delete(tool);

            return BaseResponseDTO.success("Tool deleted successfully", "Tool with ID " + id + " deleted");

        } catch (Exception e) {
            return BaseResponseDTO.error("Error deleting tool: " + e.getMessage());
        }
    }

    private ToolResponseDTO convertToDTO(Tool tool) {
        ToolResponseDTO dto = new ToolResponseDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setCategoryId(tool.getCategory().getId());
        dto.setCategoryName(tool.getCategory().getName());
        dto.setType(tool.getType().name());
        dto.setIsExpensive(tool.getIsExpensive());
        dto.setThreshold(tool.getThreshold());
        dto.setImageUrl(tool.getImageUrl());
        dto.setStatus(tool.getStatus());
        dto.setCreatedAt(tool.getCreatedAt());
        return dto;
    }




//    MANAGERS - TOOL REQUESTS - TO CENTRAL OFFICE
//    public BaseResponseDTO<ToolRequestResponseDTO> createToolRequest(CreateToolRequestDTO requestDTO) {
//        try {
//            User currentUser = securityUtil.getCurrentUser();
//            Factory factory = currentUser.getFactory();
//
//            if (factory == null) {
//                return BaseResponseDTO.error("Factory not found for current user");
//            }
//
//            // Validate all tools exist
//            for (ToolRequestDTO toolRequest : requestDTO.getTools()) {
//                if (!toolRepository.existsById(toolRequest.getToolId())) {
//                    return BaseResponseDTO.error("Tool not found with ID: " + toolRequest.getToolId());
//                }
//            }
//
//            // Create tool request
//            ToolRequest toolRequest = new ToolRequest();
//            toolRequest.setRequestedBy(currentUser);
//            toolRequest.setFactory(factory);
//            toolRequest.setStatus(ToolRequestStatus.PENDING);
//            toolRequest.setCreatedAt(LocalDateTime.now());
//
//            ToolRequest savedRequest = toolRequestRepository.save(toolRequest);
//
//            // Create request items
//            List<ToolRequestItem> requestItems = new ArrayList<>();
//            for (ToolRequestDTO toolRequestDTO : requestDTO.getTools()) {
//                Tool tool = toolRepository.findById(toolRequestDTO.getToolId())
//                        .orElseThrow(() -> new RuntimeException("Tool not found"));
//
//                ToolRequestItem item = new ToolRequestItem();
//                item.setToolRequest(savedRequest);
//                item.setTool(tool);
//                item.setQuantity(toolRequestDTO.getQuantity());
//                item.setCreatedAt(LocalDateTime.now());
//
//                requestItems.add(item);
//            }
//
//            savedRequest.setRequestItems(requestItems);
//            ToolRequest finalRequest = toolRequestRepository.save(savedRequest);
//
//            return BaseResponseDTO.success(
//                    "Tool request created successfully",
//                    convertToResponseDTO(finalRequest)
//            );
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error creating tool request: " + e.getMessage());
//        }
//    }
//
//    public BaseResponseDTO<List<ToolRequestResponseDTO>> getMyToolRequests(BaseRequestDTO request) {
//        try {
//            User currentUser = securityUtil.getCurrentUser();
//            Pageable pageable = PaginationUtil.toPageable(request);
//
//            Page<ToolRequest> requestPage = toolRequestRepository.findByRequestedBy(
//                    currentUser, pageable);
//
//            List<ToolRequestResponseDTO> responseDTOs = requestPage.getContent().stream()
//                    .map(this::convertToResponseDTO)
//                    .collect(Collectors.toList());
//
//            return BaseResponseDTO.success("Tool requests retrieved successfully", responseDTOs);
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving tool requests: " + e.getMessage());
//        }
//    }
//
//    public BaseResponseDTO<List<ToolRequestResponseDTO>> getPendingRequests(BaseRequestDTO request) {
//        try {
//            Pageable pageable = PaginationUtil.toPageable(request);
//
//            Page<ToolRequest> requestPage = toolRequestRepository.findByStatus(
//                    ToolRequestStatus.PENDING, pageable);
//
//            List<ToolRequestResponseDTO> responseDTOs = requestPage.getContent().stream()
//                    .map(this::convertToResponseDTO)
//                    .collect(Collectors.toList());
//
//            return BaseResponseDTO.success("Pending tool requests retrieved successfully", responseDTOs);
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving pending tool requests: " + e.getMessage());
//        }
//    }
//
//    public BaseResponseDTO<ToolRequestResponseDTO> approveToolRequest(Long requestId) {
//        try {
//            ToolRequest toolRequest = toolRequestRepository.findById(requestId)
//                    .orElseThrow(() -> new RuntimeException("Tool request not found"));
//
//            User currentUser = securityUtil.getCurrentUser();
//
//            // Update request status
//            toolRequest.setStatus(ToolRequestStatus.APPROVED);
//            toolRequest.setApprovedBy(currentUser);
//            toolRequest.setApprovedAt(LocalDateTime.now());
//            toolRequest.setUpdatedAt(LocalDateTime.now());
//
//            // Add tools to factory stock
//            for (ToolRequestItem item : toolRequest.getRequestItems()) {
//                factoryToolStockService.addToolsToFactoryStock(
//                        item.getTool().getId(),
//                        toolRequest.getFactory().getId(),
//                        item.getQuantity()
//                );
//            }
//
//            ToolRequest updatedRequest = toolRequestRepository.save(toolRequest);
//
//            return BaseResponseDTO.success(
//                    "Tool request approved successfully",
//                    convertToResponseDTO(updatedRequest)
//            );
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error approving tool request: " + e.getMessage());
//        }
//    }
//
//    public BaseResponseDTO<ToolRequestResponseDTO> markAsReceived(Long requestId) {
//        try {
//            ToolRequest toolRequest = toolRequestRepository.findById(requestId)
//                    .orElseThrow(() -> new RuntimeException("Tool request not found"));
//
//            if (toolRequest.getStatus() != ToolRequestStatus.APPROVED) {
//                return BaseResponseDTO.error("Only approved requests can be marked as received");
//            }
//
//            toolRequest.setStatus(ToolRequestStatus.COMPLETED);
//            toolRequest.setUpdatedAt(LocalDateTime.now());
//
//            ToolRequest updatedRequest = toolRequestRepository.save(toolRequest);
//
//            return BaseResponseDTO.success(
//                    "Tool request marked as received successfully",
//                    convertToResponseDTO(updatedRequest)
//            );
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error marking request as received: " + e.getMessage());
//        }
//    }
//
//    private ToolRequestResponseDTO convertToResponseDTO(ToolRequest toolRequest) {
//        ToolRequestResponseDTO dto = new ToolRequestResponseDTO();
//        dto.setId(toolRequest.getId());
//        dto.setFactoryName(toolRequest.getFactory().getName());
//        dto.setRequestedByName(toolRequest.getRequestedBy().getName());
//        dto.setStatus(toolRequest.getStatus());
//        dto.setCreatedAt(toolRequest.getCreatedAt());
//        dto.setApprovedAt(toolRequest.getApprovedAt());
//
//        List<ToolRequestItemDTO> itemDTOs = toolRequest.getRequestItems().stream()
//                .map(this::convertToItemDTO)
//                .collect(Collectors.toList());
//        dto.setTools(itemDTOs);
//
//        return dto;
//    }
//
//    private ToolRequestItemDTO convertToItemDTO(ToolRequestItem item) {
//        ToolRequestItemDTO dto = new ToolRequestItemDTO();
//        dto.setToolId(item.getTool().getId());
//        dto.setToolName(item.getTool().getName());
//        dto.setQuantity(item.getQuantity());
//        return dto;
//    }
}