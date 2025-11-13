package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.service.ProductRestockRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restock-requests")
public class ProductRestockRequestController {

    @Autowired
    private ProductRestockRequestService productRestockRequestService;

    // Chief Officer creates restock request
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<RestockRequestDTO>> createRestockRequest(
            @RequestBody CreateRestockRequestDTO requestDTO) {
        return ResponseEntity.ok(productRestockRequestService.createRestockRequest(requestDTO));
    }


    // Factory Manager completes restock request
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{requestId}/action")
    public ResponseEntity<BaseResponseDTO<RestockRequestDTO>> completeRestockRequest(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(productRestockRequestService.completeRestockRequest(requestId));
    }

    // Factory Manager updates stock directly (without request)
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/update-stock")
    public ResponseEntity<BaseResponseDTO<String>> updateStockDirectly(
            @RequestBody UpdateStockDTO stockDTO) {
        return ResponseEntity.ok(productRestockRequestService.updateStockDirectly(stockDTO));
    }

    // Get all restock requests with pagination and filtering (Chief Officers + Managers)
    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @GetMapping("/")
    public ResponseEntity<BaseResponseDTO<List<RestockRequestDTO>>> getAllRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            @ModelAttribute BaseRequestDTO request) {
        return ResponseEntity.ok(productRestockRequestService.getAllRestockRequests(status, request));
    }

    // Get my restock requests with pagination and filtering
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/my-requests")
    public ResponseEntity<BaseResponseDTO<List<RestockRequestDTO>>> getMyRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            @ModelAttribute BaseRequestDTO request) {
        return ResponseEntity.ok(productRestockRequestService.getMyRestockRequests(status, request));
    }

    // Get restock requests for manager's assigned factory with pagination and filtering
    @GetMapping("/my-factory-requests")
    public ResponseEntity<BaseResponseDTO<List<RestockRequestDTO>>> getMyFactoryRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            @ModelAttribute BaseRequestDTO request) {
        return ResponseEntity.ok(productRestockRequestService.getMyFactoryRestockRequests(status, request));
    }
}