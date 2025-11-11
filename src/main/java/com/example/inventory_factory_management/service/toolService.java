package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.BaseRequestDTO;
import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.ToolDTO;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.tool;
import com.example.inventory_factory_management.entity.toolCategory;
import com.example.inventory_factory_management.repository.toolCategoryRepository;
import com.example.inventory_factory_management.repository.toolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class toolService {

    @Autowired
    private toolRepository toolRepository;

    @Autowired
    private toolCategoryRepository toolCategoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public BaseResponseDTO<ToolDTO> createTool(ToolDTO toolDTO) {
        try {
            toolCategory category = toolCategoryRepository.findById(toolDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Tool category not found with id: " + toolDTO.getCategoryId()));

            // Create tool entity
            tool newTool = new tool();
            newTool.setName(toolDTO.getName());
            newTool.setCategory(category);
            newTool.setImageUrl(toolDTO.getImageUrl()); // Set the image URL
            newTool.setType(toolDTO.getType());
            newTool.setIsExpensive(toolDTO.getIsExpensive());
            newTool.setThreshold(toolDTO.getThreshold());
            newTool.setQty(toolDTO.getQty());
            newTool.setStatus(account_status.ACTIVE);

            tool savedTool = toolRepository.save(newTool);
            return BaseResponseDTO.success("Tool created successfully", convertToDTO(savedTool));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<ToolDTO>> getAllTools(BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<tool> toolPage = toolRepository.findAll(pageable);
            Page<ToolDTO> dtoPage = toolPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Tools retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve tools: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<ToolDTO>> getToolsByCategory(Long categoryId, BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<tool> toolPage = toolRepository.findByCategoryId(categoryId, pageable);
            Page<ToolDTO> dtoPage = toolPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Tools retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get tools by category: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ToolDTO> getToolById(Long id) {
        try {
            tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found with id: " + id));
            return BaseResponseDTO.success(convertToDTO(tool));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<ToolDTO> updateTool(Long id, ToolDTO toolDTO) {
        try {
            tool existingTool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found with id: " + id));

            toolCategory category = toolCategoryRepository.findById(toolDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Tool category not found with id: " + toolDTO.getCategoryId()));

            // Update image if provided
            existingTool.setImageUrl(toolDTO.getImageUrl()); // Set the image URL


            // Update other fields
            existingTool.setName(toolDTO.getName());
            existingTool.setCategory(category);
            existingTool.setType(toolDTO.getType());
            existingTool.setIsExpensive(toolDTO.getIsExpensive());
            existingTool.setThreshold(toolDTO.getThreshold());
            existingTool.setQty(toolDTO.getQty());
            existingTool.setUpdatedAt(LocalDateTime.now());

            tool updatedTool = toolRepository.save(existingTool);
            return BaseResponseDTO.success("Tool updated successfully", convertToDTO(updatedTool));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update tool: " + e.getMessage());
        }
    }

    public BaseResponseDTO<String> deleteTool(Long id) {
        try {
            tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found with id: " + id));

            tool.setStatus(account_status.INACTIVE);
            tool.setUpdatedAt(LocalDateTime.now());
            toolRepository.save(tool);

            return BaseResponseDTO.success("Tool deleted successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete tool: " + e.getMessage());
        }
    }


    private ToolDTO convertToDTO(tool tool) {
        ToolDTO dto = new ToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setCategoryId(tool.getCategory().getId());
        dto.setCategoryName(tool.getCategory().getName());
        dto.setImageUrl(tool.getImageUrl());
        dto.setType(tool.getType());
        dto.setIsExpensive(tool.getIsExpensive());
        dto.setThreshold(tool.getThreshold());
        dto.setQty(tool.getQty());
        dto.setStatus(tool.getStatus());
        dto.setCreatedAt(tool.getCreatedAt());
        dto.setUpdatedAt(tool.getUpdatedAt());
        return dto;
    }
}