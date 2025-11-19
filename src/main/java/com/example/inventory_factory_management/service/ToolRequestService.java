package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.constants.*;
import com.example.inventory_factory_management.dto.BaseRequestDTO;
import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.CreateToolRequestDTO;
import com.example.inventory_factory_management.dto.WorkerToolResponseDTO;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ToolRequestService {

    private final ToolRequestRepository toolRequestRepository;
    private final ToolRepository toolRepository;
    private final ToolStockRepository toolStockRepository;
    private final ToolStorageMappingRepository toolStorageMappingRepository;
    private final SecurityUtil securityUtil;
    private final ToolIssuanceRepository toolIssuanceRepository;

    @Autowired
    public ToolRequestService(ToolRequestRepository toolRequestRepository, ToolRepository toolRepository, ToolStockRepository toolStockRepository, ToolStorageMappingRepository toolStorageMappingRepository, SecurityUtil securityUtil, ToolIssuanceRepository toolIssuanceRepository) {
        this.toolRequestRepository = toolRequestRepository;
        this.toolRepository = toolRepository;
        this.toolStockRepository = toolStockRepository;
        this.toolStorageMappingRepository = toolStorageMappingRepository;
        this.securityUtil = securityUtil;
        this.toolIssuanceRepository = toolIssuanceRepository;
    }


    // Worker creates tool request
    @Transactional
    public BaseResponseDTO<WorkerToolResponseDTO> createToolRequest(CreateToolRequestDTO requestDTO) {
        try {
            User currentUser = securityUtil.getCurrentUser();

            if (!currentUser.getRole().equals(Role.WORKER)) {
                return BaseResponseDTO.error("Only workers can request tools");
            }

            Tool tool = toolRepository.findById(requestDTO.getToolId())
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            if(requestDTO.getQuantity() <= 0) {
                return BaseResponseDTO.error("Quantity cannot be negative");
            }
            // Get worker's factory
            Factory factory = currentUser.getUserFactories().stream()
                    .findFirst()
                    .map(UserFactory::getFactory)
                    .orElseThrow(() -> new RuntimeException("Factory not found for worker"));

            // Check stock availability
            ToolStock toolStock = toolStockRepository.findByToolIdAndFactoryFactoryId(
                            tool.getId(), factory.getFactoryId())
                    .orElseThrow(() -> new RuntimeException("Tool not available in factory stock"));

            if (toolStock.getAvailableQuantity() < requestDTO.getQuantity()) {
                return BaseResponseDTO.error("Insufficient quantity available");
            }


            ToolRequest toolRequest = new ToolRequest();
            toolRequest.setWorker(currentUser);
            toolRequest.setTool(tool);
            toolRequest.setRequestQty(requestDTO.getQuantity().longValue());
            toolRequest.setStatus(ToolOrProductRequestStatus.PENDING);
//            toolRequest.setAutoReturnDate(LocalDateTime.now().plusDays(7));

            ToolRequest savedRequest = toolRequestRepository.save(toolRequest);
            return BaseResponseDTO.success("Tool request created successfully", convertToDTO(savedRequest));

        } catch (Exception e) {
            return BaseResponseDTO.error("Error creating tool request: " + e.getMessage());
        }
    }

    // method to handle approval/rejection
    public BaseResponseDTO<String> handleToolRequest(Long requestId, String action, String rejectionReason) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            ToolRequest toolRequest = toolRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Tool request not found"));

            // Check if already processed
            if (!toolRequest.getStatus().equals(ToolOrProductRequestStatus.PENDING)) {
                return BaseResponseDTO.error("Tool request already processed");
            }

            // Check authorization based on tool type and user role
            if (toolRequest.getTool().getIsExpensive().equals(Expensive.YES)) {
                if (!currentUser.getRole().equals(Role.MANAGER)) {
                    return BaseResponseDTO.error("Only manager can handle expensive tools");
                }
            } else {
                if (!currentUser.getRole().equals(Role.CHIEF_SUPERVISOR)) {
                    return BaseResponseDTO.error("Only chief supervisor can handle non-expensive tools");
                }
            }

            if ("APPROVE".equalsIgnoreCase(action)) {
                // APPROVE LOGIC
                toolRequest.setStatus(ToolOrProductRequestStatus.APPROVED);
                toolRequest.setApprovedBy(currentUser);
                toolRequest.setUpdatedAt(LocalDateTime.now());
                toolRequestRepository.save(toolRequest);

                // Issue tool to worker
                Factory factory = toolRequest.getWorker().getUserFactories().stream()
                        .findFirst()
                        .map(UserFactory::getFactory)
                        .orElseThrow(() -> new RuntimeException("Factory not found"));

                Tool tool = toolRequest.getTool();
                Long quantity = toolRequest.getRequestQty();

                // Reduce stock
                ToolStock toolStock = toolStockRepository.findByToolIdAndFactoryFactoryId(
                                tool.getId(), factory.getFactoryId())
                        .orElseThrow(() -> new RuntimeException("Tool stock not found"));

                if (toolStock.getAvailableQuantity() < quantity) {
                    return BaseResponseDTO.error("Insufficient quantity available");
                }

                // Reduce storage quantity
                toolStock.setAvailableQuantity(toolStock.getAvailableQuantity() - quantity);
                toolStock.setIssuedQuantity(toolStock.getIssuedQuantity() + quantity);
                toolStockRepository.save(toolStock);

                List<ToolStorageMapping> storageMappings = toolStorageMappingRepository
                        .findByToolIdAndFactoryFactoryId(tool.getId(), factory.getFactoryId());

                Long remainingQuantity = quantity;
                for (ToolStorageMapping mapping : storageMappings) {
                    if (remainingQuantity <= 0) break;

                    Long deductQuantity = Math.min(remainingQuantity, mapping.getQuantity().longValue());
                    mapping.setQuantity(mapping.getQuantity() - deductQuantity.intValue());
                    remainingQuantity -= deductQuantity;
                    toolStorageMappingRepository.save(mapping);
                }

                // Create issuance record
                ToolIssuance issuance = new ToolIssuance();
                issuance.setRequest(toolRequest);
                issuance.setStatus(ToolIssuanceStatus.ISSUED);
                toolIssuanceRepository.save(issuance);

                // Update request with issued time
                toolRequest.setIssuedAt(LocalDateTime.now());
                toolRequestRepository.save(toolRequest);

                return BaseResponseDTO.success("Tool request approved and issued successfully");

            } else if ("REJECT".equalsIgnoreCase(action)) {
                // REJECT LOGIC
                if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                    return BaseResponseDTO.error("Rejection reason is required");
                }

                toolRequest.setStatus(ToolOrProductRequestStatus.REJECTED);
                toolRequest.setApprovedBy(currentUser);
                toolRequest.setRejectionReason(rejectionReason);
                toolRequest.setUpdatedAt(LocalDateTime.now());
                toolRequestRepository.save(toolRequest);

                return BaseResponseDTO.success("Tool request rejected successfully");

            } else {
                return BaseResponseDTO.error("Invalid action. Use 'APPROVE' or 'REJECT'");
            }

        } catch (Exception e) {
            return BaseResponseDTO.error("Error handling tool request: " + e.getMessage());
        }
    }

    // Get pending requests for approval/rejection
    public BaseResponseDTO<Page<WorkerToolResponseDTO>> getPendingRequests(BaseRequestDTO request) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Pageable pageable = PaginationUtil.toPageable(request);

//            if(currentUser.getRole().equals(Role.WORKER)){
//                return BaseResponseDTO.error("Not authorized to view pending requests");
//            }

            Page<ToolRequest> pendingRequests;

            if (currentUser.getRole().equals(Role.CHIEF_SUPERVISOR)) {
                // Chief supervisor gets non-expensive tool requests
                pendingRequests = toolRequestRepository.findByStatusAndToolIsExpensive(
                        ToolOrProductRequestStatus.PENDING, Expensive.NO, pageable);
            } else if (currentUser.getRole().equals(Role.MANAGER)) {
                // Manager gets expensive tool requests
                pendingRequests = toolRequestRepository.findByStatusAndToolIsExpensive(
                        ToolOrProductRequestStatus.PENDING, Expensive.YES, pageable);
            } else {
                return BaseResponseDTO.error("Not authorized to view pending requests");
            }

            Page<WorkerToolResponseDTO> dtos = pendingRequests.map(this::convertToDTO);
            return BaseResponseDTO.success("Pending requests retrieved successfully", dtos);

        } catch (Exception e) {
            return BaseResponseDTO.error("Error retrieving pending requests: " + e.getMessage());
        }
    }


    // Get worker's own requests
    public BaseResponseDTO<Page<WorkerToolResponseDTO>> getMyRequests(BaseRequestDTO request) {
        try {
            User currentUser = securityUtil.getCurrentUser();
            Pageable pageable = PaginationUtil.toPageable(request, "createdAt");

            Page<ToolRequest> myRequests = toolRequestRepository
                    .findByWorkerUserId(currentUser.getUserId(), pageable);

            Page<WorkerToolResponseDTO> dtos = myRequests.map(this::convertToDTO);
            return BaseResponseDTO.success("Your tool requests retrieved successfully", dtos);

        } catch (Exception e) {
            return BaseResponseDTO.error("Error retrieving your requests: " + e.getMessage());
        }
    }


    private WorkerToolResponseDTO convertToDTO(ToolRequest request) {
        WorkerToolResponseDTO dto = new WorkerToolResponseDTO();
        dto.setId(request.getId());
        dto.setToolId(request.getTool().getId());
        dto.setToolName(request.getTool().getName());
        dto.setToolType(request.getTool().getType().name());
        dto.setIsExpensive(request.getTool().getIsExpensive().name());
        dto.setRequestQty(request.getRequestQty());
        dto.setStatus(request.getStatus());
        dto.setWorkerName(request.getWorker().getUsername());
        dto.setApprovedBy(request.getApprovedBy() != null ? request.getApprovedBy().getUsername() : null);
        dto.setCreatedAt(request.getCreatedAt());
        dto.setIssuedAt(request.getIssuedAt());
        dto.setReturnedAt(request.getReturnedAt());
        dto.setRejectionReason(request.getRejectionReason());
//        dto.setAutoReturnDate(request.getAutoReturnDate());
        return dto;
    }


}
