package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner/managers")
public class OwnerController {

    @Autowired
    private ManagerService managerService;

    // Create a new manager
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<UserDTO>> createManager(@Valid @RequestBody UserDTO managerDTO) {
        BaseResponseDTO<UserDTO> response = managerService.createManager(managerDTO);
        return ResponseEntity.ok(response);
    }

    // Get all managers with pagination and filtering
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/")
    public ResponseEntity<BaseResponseDTO<Page<UserDTO>>> getAllManagers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<UserDTO>> response = managerService.getAllManagers(search, status, request);
        return ResponseEntity.ok(response);
    }

    // Get manager by ID
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/{managerId}")
    public ResponseEntity<BaseResponseDTO<UserDTO>> getManagerById(@PathVariable Long managerId) {
        BaseResponseDTO<UserDTO> response = managerService.getManagerById(managerId);
        return ResponseEntity.ok(response);
    }

    // Get manager by name (exact match)
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/name/{managerName}")
    public ResponseEntity<BaseResponseDTO<UserDTO>> getManagerByName(@PathVariable String managerName) {
        BaseResponseDTO<UserDTO> response = managerService.getManagerByName(managerName);
        return ResponseEntity.ok(response);
    }

    // Search managers by name (partial match)
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/search/{managerName}")
    public ResponseEntity<BaseResponseDTO<Page<UserDTO>>> searchManagersByName(
            @PathVariable String managerName,
            @Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<UserDTO>> response = managerService.searchManagersByName(managerName, request);
        return ResponseEntity.ok(response);
    }

    // Update manager details
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{managerId}")
    public ResponseEntity<BaseResponseDTO<UserDTO>> updateManager(
            @PathVariable Long managerId,
            @Valid @RequestBody UserUpdateDTO managerDTO) {
        BaseResponseDTO<UserDTO> response = managerService.updateManager(managerId, managerDTO);
        return ResponseEntity.ok(response);
    }

    // Delete manager (soft delete)
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{managerId}")
    public ResponseEntity<BaseResponseDTO<String>> deleteManager(@PathVariable Long managerId) {
        BaseResponseDTO<String> response = managerService.deleteManager(managerId);
        return ResponseEntity.ok(response);
    }

    // Toggle manager status (activate/deactivate)
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/{managerId}/toggle-status")
    public ResponseEntity<BaseResponseDTO<String>> toggleManagerStatus(@PathVariable Long managerId) {
        BaseResponseDTO<String> response = managerService.toggleManagerStatus(managerId);
        return ResponseEntity.ok(response);
    }

    // Get available managers (without factory assignment)
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/available")
    public ResponseEntity<BaseResponseDTO<Page<UserDTO>>> getAvailableManagers(@Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<UserDTO>> response = managerService.getAvailableManagers(request);
        return ResponseEntity.ok(response);
    }
}