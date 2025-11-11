package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.service.toolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tools")
public class toolController {

    @Autowired
    private toolService toolService;


    @PostMapping
    public ResponseEntity<BaseResponseDTO<ToolDTO>> createTool(@Valid @RequestBody ToolDTO toolDTO) {
        BaseResponseDTO<ToolDTO> response = toolService.createTool(toolDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponseDTO<Page<ToolDTO>>> getAllTools(@Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<ToolDTO>> response = toolService.getAllTools(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ToolDTO>> getToolById(@PathVariable Long id) {
        BaseResponseDTO<ToolDTO> response = toolService.getToolById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ToolDTO>> updateTool(@PathVariable Long id, @Valid @RequestBody ToolDTO toolDTO) {
        BaseResponseDTO<ToolDTO> response = toolService.updateTool(id, toolDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<String>> deleteTool(@PathVariable Long id) {
        BaseResponseDTO<String> response = toolService.deleteTool(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BaseResponseDTO<Page<ToolDTO>>> getToolsByCategory(
            @PathVariable Long categoryId,
            @Valid BaseRequestDTO request) {
        BaseResponseDTO<Page<ToolDTO>> response = toolService.getToolsByCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }
}
