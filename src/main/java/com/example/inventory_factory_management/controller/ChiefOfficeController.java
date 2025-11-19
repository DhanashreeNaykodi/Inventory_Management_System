package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.constants.OrderStatus;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.DistributorOrderRequest;
import com.example.inventory_factory_management.service.CentralOfficeService;
import com.example.inventory_factory_management.service.OrderService;
import com.example.inventory_factory_management.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/central-office")
    public BaseResponseDTO<Void> createCentralOffice( @RequestBody CentralOfficeDTO centralOfficeDto) {
        return centralOfficeService.createCentralOffice(centralOfficeDto);
    }

    @PostMapping("/add-central-officer")
    public BaseResponseDTO<Void> addCentralOfficer(@Valid @RequestBody AddChiefOfficerDTO addCentralOfficerDto) {
        return centralOfficeService.addChiefOfficerToOffice(addCentralOfficerDto);
    }

    @GetMapping("/central-officer")
    public BaseResponseDTO<List<CentralOfficeResponseDTO>> getCentralOfficers() {
        return centralOfficeService.getCentralOfficers();
    }


    @DeleteMapping("/central-office/central-officer/{centralOfficerId}")
    public BaseResponseDTO<Void> removeCentralOfficer(@PathVariable Long centralOfficerId) {
        return centralOfficeService.removeChiefOfficerFromOffice(centralOfficerId);
    }


    // NEW: Search central officers by name
    @GetMapping("/central-officers/search")
    public ResponseEntity<BaseResponseDTO<List<CentralOfficerDTO>>> searchCentralOfficersByName(
            @RequestParam String name) {
        BaseResponseDTO<List<CentralOfficerDTO>> response = centralOfficeService.searchCentralOfficersByName(name);
        return ResponseEntity.ok(response);
    }

    // NEW: Remove chief officer by name
    @DeleteMapping("/central-officer/by-name")
    public ResponseEntity<BaseResponseDTO<Void>> removeCentralOfficerByName(
            @RequestParam String officerName) {
        BaseResponseDTO<Void> response = centralOfficeService.removeChiefOfficerByName(officerName);
        return ResponseEntity.ok(response);
    }


}