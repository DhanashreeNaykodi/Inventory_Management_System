package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.CentralOfficeService;
import com.example.inventory_factory_management.service.OrderService;
import com.example.inventory_factory_management.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner")
public class ChiefOfficeController {

    private CentralOfficeService centralOfficeService;
    private OrderService orderService;
    private ProductService productService;

    @Autowired
    public ChiefOfficeController(CentralOfficeService centralOfficeService, OrderService orderService, ProductService productService) {
        this.centralOfficeService = centralOfficeService;
        this.orderService = orderService;
        this.productService = productService;
    }

    // Central Office Management Endpoints
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/central-office")
    public BaseResponseDTO<Void> createCentralOffice( @RequestBody CentralOfficeDTO centralOfficeDto) {
        return centralOfficeService.createCentralOffice(centralOfficeDto);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/add-central-officer")
    public BaseResponseDTO<Void> addCentralOfficer(@Valid @RequestBody AddChiefOfficerDTO addCentralOfficerDto) {
        return centralOfficeService.addChiefOfficerToOffice(addCentralOfficerDto);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/central-officer")
    public BaseResponseDTO<List<CentralOfficeResponseDTO>> getCentralOfficers() {
        return centralOfficeService.getCentralOfficers();
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/central-office/central-officer/{centralOfficerId}")
    public BaseResponseDTO<Void> removeCentralOfficer(@PathVariable Long centralOfficerId) {
        return centralOfficeService.removeChiefOfficerFromOffice(centralOfficerId);
    }


    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/central-officers/search")
    public ResponseEntity<BaseResponseDTO<List<CentralOfficerDTO>>> searchCentralOfficersByName(
            @RequestParam String name) {
        BaseResponseDTO<List<CentralOfficerDTO>> response = centralOfficeService.searchCentralOfficersByName(name);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/central-officer/by-name")
    public ResponseEntity<BaseResponseDTO<Void>> removeCentralOfficerByName(
            @RequestParam String officerName) {
        BaseResponseDTO<Void> response = centralOfficeService.removeChiefOfficerByName(officerName);
        return ResponseEntity.ok(response);
    }


}