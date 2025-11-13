package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.FactoryDTO;
import com.example.inventory_factory_management.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/factory/details")
    public ResponseEntity<BaseResponseDTO<FactoryDTO>> getManagerFactory() {
        BaseResponseDTO<FactoryDTO> response = managerService.getManagerFactory();
        return ResponseEntity.ok(response);
    }
}
