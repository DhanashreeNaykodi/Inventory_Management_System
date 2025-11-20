package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.constants.*;
import com.example.inventory_factory_management.dto.BaseRequestDTO;
import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.CreateToolRequestDTO;
import com.example.inventory_factory_management.dto.WorkerToolResponseDTO;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.exceptions.InvalidActionException;
import com.example.inventory_factory_management.exceptions.ResourceNotFoundException;
import com.example.inventory_factory_management.exceptions.UnauthorizedAccessException;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ToolRequestService {

    private final ToolRequestRepository toolRequestRepository;
    private final ToolRepository toolRepository;
    private final ToolStockRepository toolStockRepository;

    private final SecurityUtil securityUtil;


    @Autowired
    public ToolRequestService(ToolRequestRepository toolRequestRepository, ToolRepository toolRepository, ToolStockRepository toolStockRepository, SecurityUtil securityUtil, ToolIssuanceRepository toolIssuanceRepository) {
        this.toolRequestRepository = toolRequestRepository;
        this.toolRepository = toolRepository;
        this.toolStockRepository = toolStockRepository;
        this.securityUtil = securityUtil;
    }


    // Worker creates tool request
    @Transactional
    public BaseResponseDTO<WorkerToolResponseDTO> createToolRequest(CreateToolRequestDTO requestDTO) {

            User currentUser = securityUtil.getCurrentUser();
            if (!currentUser.getRole().equals(Role.WORKER)) {
                throw new UnauthorizedAccessException("Only workers can request tools");
            }

            Tool tool = toolRepository.findById(requestDTO.getToolId()).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
            if(requestDTO.getQuantity() <= 0) {
                throw new InvalidActionException("Quantity cannot be negative");
            }
            Factory factory = currentUser.getUserFactories().stream().findFirst().map(UserFactory::getFactory)
                    .orElseThrow(() -> new ResourceNotFoundException("Factory not found for worker"));

            ToolStock toolStock = toolStockRepository.findByTool_IdAndFactory_FactoryId(tool.getId(), factory.getFactoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tool not available in factory stock"));

            if (toolStock.getAvailableQuantity() < requestDTO.getQuantity()) {
                throw new ResourceNotFoundException("Insufficient quantity available");
            }
            ToolRequest toolRequest = new ToolRequest();
            toolRequest.setWorker(currentUser);
            toolRequest.setTool(tool);
            toolRequest.setRequestQty(requestDTO.getQuantity().longValue());
            toolRequest.setStatus(ToolOrProductRequestStatus.PENDING);

            ToolRequest savedRequest = toolRequestRepository.save(toolRequest);
            return BaseResponseDTO.success("Tool request created successfully", convertToDTO(savedRequest));
    }


    // Get pending requests for approval/rejection
    public BaseResponseDTO<Page<WorkerToolResponseDTO>> getPendingRequests(BaseRequestDTO request) {
            User currentUser = securityUtil.getCurrentUser();
            Pageable pageable = PaginationUtil.toPageable(request);
            Page<ToolRequest> pendingRequests;

            if (currentUser.getRole().equals(Role.CHIEF_SUPERVISOR)) {
                // Chief supervisor gets non-expensive tool requests
                pendingRequests = toolRequestRepository.findByStatusAndTool_IsExpensive(ToolOrProductRequestStatus.PENDING, Expensive.NO, pageable);
            } else if (currentUser.getRole().equals(Role.MANAGER)) {
                // Manager gets expensive tool requests
                pendingRequests = toolRequestRepository.findByStatusAndTool_IsExpensive(ToolOrProductRequestStatus.PENDING, Expensive.YES, pageable);
            } else {
                throw new UnauthorizedAccessException("Not authorized to view pending requests");
            }
            Page<WorkerToolResponseDTO> dtos = pendingRequests.map(this::convertToDTO);
            return BaseResponseDTO.success("Pending requests retrieved successfully", dtos);

    }


    // Get worker's own requests
    public BaseResponseDTO<Page<WorkerToolResponseDTO>> getMyRequests(BaseRequestDTO request) {
            User currentUser = securityUtil.getCurrentUser();
            Pageable pageable = PaginationUtil.toPageable(request, "createdAt");

            Page<ToolRequest> myRequests = toolRequestRepository.findByWorkerUserId(currentUser.getUserId(), pageable);

            Page<WorkerToolResponseDTO> dtos = myRequests.map(this::convertToDTO);
            return BaseResponseDTO.success("Your tool requests retrieved successfully", dtos);

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
        return dto;
    }


}
