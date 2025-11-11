package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.service.factoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner")
public class factoryController {

    @Autowired
    factoryService factoryService;

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/factories")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> getAllFactories(@Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.getAllFactories(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/factories/search")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> searchFactories(
            @RequestParam String name,
            @Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.searchFactoriesByName(name, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/factories/managers")
    public ResponseEntity<BaseResponseDTO<Page<UserDTO>>> getAllManagers(@Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<UserDTO>> response = factoryService.getAllManagers(request);
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/createFactory")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> createFactory(@Valid @RequestBody FactoryDTO factoryDTO) {
        BaseResponseDTO<FactoryDTO> response = factoryService.createFactory(factoryDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/factories/{factory_id}")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> getFactory(@PathVariable Long factory_id) {
        BaseResponseDTO<FactoryDTO> response = factoryService.getFactoryById(factory_id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteFactory/{id}")
    public ResponseEntity<BaseResponseDTO<String>> deleteFactory(@PathVariable Long id) {
        BaseResponseDTO<String> response = factoryService.deleteFactory(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/factories/filter")
    public ResponseEntity<BaseResponseDTO<Page<FactoryDTO>>> filterFactoriesByCity(
            @RequestParam String city,
            @Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<FactoryDTO>> response = factoryService.getFactoriesByCity(city, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @PatchMapping("/factories/{factory_id}/manager")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> updateFactoryManager(
            @PathVariable Long factory_id,
            @Valid @RequestBody ManagerUpdateRequest request) {
        BaseResponseDTO<FactoryDTO> response = factoryService.updateFactoryManager(factory_id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'CHIEF_OFFICER')")
    @GetMapping("/count")
    public ResponseEntity<BaseResponseDTO<CountResponseDTO>> getCount() {
        BaseResponseDTO<CountResponseDTO> response = factoryService.getFactoriesCount();
        return ResponseEntity.ok(response);
    }


}
