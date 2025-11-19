package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.FactoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/owner")
public class FactoryController {

    @Autowired
    FactoryService factoryService;

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/factories")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> getAllFactories(@Valid @ModelAttribute BaseRequestDTO request) {   //query parameters map to your BaseRequestDTO
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.getAllFactories(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/factories/search")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> searchFactories(
            @RequestParam String name,
            @Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.searchFactoriesByName(name, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/factories/managers")
    public ResponseEntity<BaseResponseDTO<Page<UserDTO>>> getAllManagers(@Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<UserDTO>> response = factoryService.getAllManagers(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/createFactory")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> createFactory(@RequestBody FactoryDTO factoryDTO) {
        BaseResponseDTO<FactoryDTO> response = factoryService.createFactory(factoryDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/factories/{factory_id}")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> getFactory(@PathVariable Long factory_id) {
        BaseResponseDTO<FactoryDTO> response = factoryService.getFactoryById(factory_id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/deleteFactory/{id}")
    public ResponseEntity<BaseResponseDTO<String>> deleteFactory(@PathVariable Long id) {
        BaseResponseDTO<String> response = factoryService.deleteFactory(id);
        return ResponseEntity.ok(response);
    }


    // For ID-based toggle
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("factory/{id}/toggle-status")
    public ResponseEntity<BaseResponseDTO<String>> toggleFactoryStatusById(@PathVariable Long id) {
        BaseResponseDTO<String> response = factoryService.toggleFactoryStatus(id);
        return ResponseEntity.ok(response);
    }

    // For name-based toggle - use different URL pattern
    @PutMapping("/name/{factoryName}/toggle-status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BaseResponseDTO<String>> toggleFactoryStatusByName(@PathVariable String factoryName) {
        BaseResponseDTO<String> response = factoryService.toggleFactoryStatus(factoryName);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/factories/filter")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> filterFactoriesByCity(
            @RequestParam String city,
            @Valid @ModelAttribute BaseRequestDTO request) {
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.getFactoriesByCity(city, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/factories/{factory_id}/manager")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> updateFactoryManager(
            @PathVariable Long factory_id,
            @Valid @RequestBody ManagerUpdateRequest request) {
        BaseResponseDTO<FactoryDTO> response = factoryService.updateFactoryManager(factory_id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/count")
    public ResponseEntity<BaseResponseDTO<CountResponseDTO>> getCount() {
        BaseResponseDTO<CountResponseDTO> response = factoryService.getFactoriesCount();
        return ResponseEntity.ok(response);
    }


}
