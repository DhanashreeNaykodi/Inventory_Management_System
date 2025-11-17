package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.service.ProductRestockRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class ProductRestockAndInventoryController {

    @Autowired
    private ProductRestockRequestService productRestockRequestService;


    @GetMapping("/central-office")
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    public BaseResponseDTO<Page<CentralOfficeInventoryDTO>> getCentralOfficeInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long minQuantity,
            @RequestParam(required = false) Long maxQuantity,
            BaseRequestDTO requestDTO) {

        return productRestockRequestService.getCentralOfficeInventory(
                productId, productName, minQuantity, maxQuantity, requestDTO);
    }

//     Central Officer creates restock request for a factory
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @PostMapping("/central-office/restock-requests")
    public BaseResponseDTO<CentralOfficeRestockResponseDTO> createRestockRequest(
            @RequestBody CreateRestockRequestDTO requestDTO) {
        return productRestockRequestService.createRestockRequest(requestDTO);
    }

    // Factory manager completes restock request (transfers stock to central office)
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/factories/restock-requests/{requestId}/complete")
    public BaseResponseDTO<FactoryRestockResponseDTO> completeRestockRequest(
            @PathVariable Long requestId) {
        return productRestockRequestService.completeRestockRequest(requestId);
    }

    // Factory manager updates stock directly (production in factory)
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/factories/stock/production")
    public BaseResponseDTO<String> updateStockDirectly(
            @RequestBody UpdateProductStockDTO stockDTO) {
        return productRestockRequestService.updateStockDirectly(stockDTO);
    }

    // Central Officers - Get restock requests created by me
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @GetMapping("/central-office/my-restock-requests")
    public BaseResponseDTO<Page<CentralOfficeRestockResponseDTO>> getMyRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            BaseRequestDTO requestDTO) {
        return productRestockRequestService.getMyRestockRequests(status, requestDTO);
    }

    // Managers - Get restock requests for their assigned factory
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/factories/my-restock-requests")
    public BaseResponseDTO<Page<FactoryRestockResponseDTO>> getMyFactoryRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            @ModelAttribute BaseRequestDTO requestDTO) {
        return productRestockRequestService.getMyFactoryRestockRequests(status, requestDTO);
    }
}