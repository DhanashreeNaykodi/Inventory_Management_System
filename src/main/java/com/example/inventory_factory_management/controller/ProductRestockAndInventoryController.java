package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.service.ProductRestockRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class ProductRestockAndInventoryController {

    @Autowired
    private ProductRestockRequestService productRestockRequestService;

    // Central Officer creates restock request for a factory
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @PostMapping("/central-office/restock-requests")  //make is createRestock
    public ResponseEntity<BaseResponseDTO<CentralOfficeRestockResponseDTO>> createRestockRequest(
            @RequestBody CreateRestockRequestDTO requestDTO) {
        return ResponseEntity.ok(productRestockRequestService.createRestockRequest(requestDTO));
    }

    // Factory manager completes restock request (transfers stock to central office)
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/factories/restock-requests/{requestId}/complete")
    public ResponseEntity<BaseResponseDTO<FactoryRestockResponseDTO>> completeRestockRequest(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(productRestockRequestService.completeRestockRequest(requestId));
    }

    // Factory manager updates stock directly (production in factory)
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/factories/stock/production")
    public ResponseEntity<BaseResponseDTO<String>> updateStockDirectly(
            @RequestBody UpdateStockDTO stockDTO) {
        return ResponseEntity.ok(productRestockRequestService.updateStockDirectly(stockDTO));
    }

    // Central Officers - Get all restock requests across all factories
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @GetMapping("/central-office/restock-requests")
    public ResponseEntity<BaseResponseDTO<Page<CentralOfficeRestockResponseDTO>>> getAllRestockRequests(
            @RequestParam(required = false) RequestStatus status) {
        BaseRequestDTO request = new BaseRequestDTO();
        return ResponseEntity.ok(productRestockRequestService.getAllRestockRequests(status, request));
    }

    // Central Officers - Get restock requests created by me
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @GetMapping("/central-office/my-restock-requests")
    public ResponseEntity<BaseResponseDTO<Page<CentralOfficeRestockResponseDTO>>> getMyRestockRequests(
            @RequestParam(required = false) RequestStatus status) {
        BaseRequestDTO request = new BaseRequestDTO();
        return ResponseEntity.ok(productRestockRequestService.getMyRestockRequests(status, request));
    }

    // Managers - Get restock requests for their assigned factory
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/factories/my-restock-requests")
    public ResponseEntity<BaseResponseDTO<Page<FactoryRestockResponseDTO>>> getMyFactoryRestockRequests(
            @RequestParam(required = false) RequestStatus status) {
        BaseRequestDTO request = new BaseRequestDTO();
        return ResponseEntity.ok(productRestockRequestService.getMyFactoryRestockRequests(status, request));
    }

    // Central Office - Get inventory for specific product
    @PreAuthorize("hasAnyRole('CENTRAL_OFFICER', 'OWNER')")
    @GetMapping("/central-office/inventory/products/{productId}")
    public ResponseEntity<BaseResponseDTO<CentralOfficeInventoryDTO>> getCentralOfficeInventory(
            @PathVariable Long productId) {
        return ResponseEntity.ok(productRestockRequestService.getCentralOfficeInventory(productId));
    }

    // Central Office - Get all inventory with pagination
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/central-office")
    public ResponseEntity<BaseResponseDTO<Page<CentralOfficeInventoryDTO>>> getAllCentralOfficeInventory() {
        BaseRequestDTO request = new BaseRequestDTO();
        return ResponseEntity.ok(productRestockRequestService.getAllCentralOfficeInventory(request));
    }
}