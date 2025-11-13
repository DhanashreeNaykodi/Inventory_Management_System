//package com.example.inventory_factory_management.service;
//
//
//import com.example.inventory_factory_management.DTO.*;
//import com.example.inventory_factory_management.entity.*;
//import com.example.inventory_factory_management.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class toolService {
//
//    private ToolRepository toolRepository;
//    private toolCategoryRepository toolCategoryRepository;
//    private StorageAreaRepository storageAreaRepository;
//    private ToolStorageMappingRepository toolStorageMappingRepository;
//    private FactoryRepository factoryRepository;
//
//    @Autowired
//    public toolService(ToolRepository toolRepository, toolCategoryRepository toolCategoryRepository, StorageAreaRepository storageAreaRepository, ToolStorageMappingRepository toolStorageMappingRepository, FactoryRepository factoryRepository) {
//        this.toolRepository = toolRepository;
//        this.toolCategoryRepository = toolCategoryRepository;
//        this.storageAreaRepository = storageAreaRepository;
//        this.toolStorageMappingRepository = toolStorageMappingRepository;
//        this.factoryRepository = factoryRepository;
//    }
//
//    public BaseResponseDTO<ToolResponseDTO> addTool(AddNewToolDTO addNewToolDTO, MultipartFile image) throws IOException {
//        try {
//            if (toolRepository.existsByNameIgnoreCase(addNewToolDTO.getName())) {
//                return BaseResponseDTO.error("Tool with this name already exists");
//            }
//
//            Optional<toolCategory> category = toolCategoryRepository.findById(addNewToolDTO.getCategoryId());
//            if (category.isEmpty()) {
//                return BaseResponseDTO.error("Category not found");
//            }
//
//            tool tool = new tool();
//            tool.setName(addNewToolDTO.getName());
//            tool.setCategory(category.get());
//            tool.setType(addNewToolDTO.getType());
//            tool.setIsExpensive(addNewToolDTO.getIsExpensive());
//            tool.setThreshold(addNewToolDTO.getThreshold());
//            tool.setQty(addNewToolDTO.getQuantity() != null ? addNewToolDTO.getQuantity() : 0);
//
//            if (image != null && !image.isEmpty()) {
////                tool.setImageUrl(image.getOriginalFilename());
//                tool.setImageUrl(image.getOriginalFilename());
//            }
//
//            tool.setCreatedAt(LocalDateTime.now());
//            tool.setUpdatedAt(LocalDateTime.now());
//
//            tool savedTool = toolRepository.save(tool);
//
//            if (addNewToolDTO.getStorageAreaId() != null && addNewToolDTO.getQuantity() != null) {
//                assignToolToStorageArea(savedTool, addNewToolDTO.getStorageAreaId(), addNewToolDTO.getQuantity());
//            }
//
//            return BaseResponseDTO.success("Tool added successfully", convertToToolResponseDTO(savedTool));
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error adding tool: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<List<ToolResponseDTO>> getAllTools() {
//        try {
//            List<tool> tools = toolRepository.findAll();
//            List<ToolResponseDTO> toolResponseDTOs = tools.stream()
//                    .map(this::convertToToolResponseDTO)
//                    .collect(Collectors.toList());
//            return BaseResponseDTO.success("Tools retrieved successfully", toolResponseDTOs);
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving tools: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<ToolResponseDTO> getToolById(Long id) {
//        try {
//            Optional<tool> tool = toolRepository.findById(id);
//            if (tool.isEmpty()) {
//                return BaseResponseDTO.error("Tool not found");
//            }
//            return BaseResponseDTO.success("Tool retrieved successfully", convertToToolResponseDTO(tool.get()));
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving tool: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<ToolResponseDTO> updateTool(Long id, AddNewToolDTO addNewToolDTO, MultipartFile image) throws IOException {
//        try {
//            Optional<tool> existingTool = toolRepository.findById(id);
//            if (existingTool.isEmpty()) {
//                return BaseResponseDTO.error("Tool not found");
//            }
//
//            tool tool = existingTool.get();
//            tool.setName(addNewToolDTO.getName());
//            tool.setType(addNewToolDTO.getType());
//            tool.setIsExpensive(addNewToolDTO.getIsExpensive());
//            tool.setThreshold(addNewToolDTO.getThreshold());
//
//            if (addNewToolDTO.getQuantity() != null) {
//                tool.setQty(addNewToolDTO.getQuantity());
//            }
//
//            if (addNewToolDTO.getCategoryId() != null) {
//                Optional<toolCategory> category = toolCategoryRepository.findById(addNewToolDTO.getCategoryId());
//                category.ifPresent(tool::setCategory);
//            }
//
//            if (image != null && !image.isEmpty()) {
//                tool.setImageUrl(image.getOriginalFilename());
//            }
//
//            tool.setUpdatedAt(LocalDateTime.now());
//
//            tool updatedTool = toolRepository.save(tool);
//            return BaseResponseDTO.success("Tool updated successfully", convertToToolResponseDTO(updatedTool));
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error updating tool: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<String> deleteTool(Long id) {
//        try {
//            if (!toolRepository.existsById(id)) {
//                return BaseResponseDTO.error("Tool not found");
//            }
//            toolRepository.deleteById(id);
//            return BaseResponseDTO.success("Tool deleted successfully", "Tool with ID " + id + " deleted");
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error deleting tool: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<List<ToolResponseDTO>> getToolsByCategory(Long categoryId) {
//        try {
//            Optional<toolCategory> category = toolCategoryRepository.findById(categoryId);
//            if (category.isEmpty()) {
//                return BaseResponseDTO.error("Category not found");
//            }
//
//            List<tool> tools = toolRepository.findByCategory(category.get());
//            List<ToolResponseDTO> toolResponseDTOs = tools.stream()
//                    .map(this::convertToToolResponseDTO)
//                    .collect(Collectors.toList());
//            return BaseResponseDTO.success("Tools retrieved successfully", toolResponseDTOs);
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving tools by category: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<String> createStorageArea(CreateStorageAreaDTO dto) {
//        try {
//            boolean exists = storageAreaRepository.existsByRowNumAndColNumAndStack(
//                    dto.getRowNum(), dto.getColNum(), dto.getStack());
//            if (exists) {
//                return BaseResponseDTO.error("Storage area with these coordinates already exists");
//            }
//
//            storageArea storageArea = new storageArea();
//            storageArea.setRowNum(dto.getRowNum());
//            storageArea.setColNum(dto.getColNum());
//            storageArea.setStack(dto.getStack());
//            storageArea.setBucket(generateBucketName(dto.getRowNum(), dto.getColNum(), dto.getStack()));
//            storageArea.setStatus("ACTIVE");
//            storageArea.setCreatedAt(LocalDateTime.now());
//            storageArea.setUpdatedAt(LocalDateTime.now());
//
//            storageAreaRepository.save(storageArea);
//            return BaseResponseDTO.success("Storage area created successfully", "Storage area created with ID: " + storageArea.getId());
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error creating storage area: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<List<StorageAreaResponseDTO>> getAllStorageAreasForPlantHead() {
//        try {
//            List<storageArea> storageAreas = storageAreaRepository.findAll();
//            List<StorageAreaResponseDTO> storageAreaResponseDTOs = storageAreas.stream()
//                    .map(this::convertToStorageAreaResponseDTO)
//                    .collect(Collectors.toList());
//            return BaseResponseDTO.success("Storage areas retrieved successfully", storageAreaResponseDTOs);
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error retrieving storage areas: " + e.getMessage());
//        }
//    }
//
//
//    public BaseResponseDTO<ToolResponseDTO> assignToolToFactory(AssignToolToFactoryDTO dto) {
//        try {
//            Optional<tool> tool = toolRepository.findById(dto.getToolId());
//            if (tool.isEmpty()) {
//                return BaseResponseDTO.error("Tool not found");
//            }
//
//            Optional<storageArea> storageArea = storageAreaRepository.findById(dto.getStorageAreaId());
//            if (storageArea.isEmpty()) {
//                return BaseResponseDTO.error("Storage area not found");
//            }
//
//            factory factory = storageArea.get().getFactory();
//
//            Optional<ToolStorageMapping> existingMapping = toolStorageMappingRepository.findByFactoryAndTool(factory, tool.get());
//
//            ToolStorageMapping mapping;
//            if (existingMapping.isPresent()) {
//                mapping = existingMapping.get();
//                mapping.setQuantity(mapping.getQuantity() + dto.getQuantity());
//            } else {
//                mapping = new ToolStorageMapping();
//                mapping.setTool(tool.get());
//                mapping.setFactory(factory);
//                mapping.setStorageArea(storageArea.get());
//                mapping.setQuantity(dto.getQuantity());
//                mapping.setCreatedAt(LocalDateTime.now());
//            }
//            mapping.setUpdatedAt(LocalDateTime.now());
//
//            toolStorageMappingRepository.save(mapping);
//
//            tool.get().setQty(tool.get().getQty() + dto.getQuantity());
//            toolRepository.save(tool.get());
//
//            return BaseResponseDTO.success("Tool assigned to factory successfully", convertToToolResponseDTO(tool.get()));
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Error assigning tool to factory: " + e.getMessage());
//        }
//    }
//
//    private void assignToolToStorageArea(tool tool, Long storageAreaId, Integer quantity) {
//        try {
//            Optional<storageArea> storageArea = storageAreaRepository.findById(storageAreaId);
//            if (storageArea.isPresent()) {
//                ToolStorageMapping mapping = new ToolStorageMapping();
//                mapping.setTool(tool);
//                mapping.setStorageArea(storageArea.get());
//                mapping.setFactory(storageArea.get().getFactory());
//                mapping.setQuantity(quantity);
//                mapping.setCreatedAt(LocalDateTime.now());
//                mapping.setUpdatedAt(LocalDateTime.now());
//                toolStorageMappingRepository.save(mapping);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Error assigning tool to storage area: " + e.getMessage());
//        }
//    }
//
//    private String generateBucketName(Integer rowNum, Integer colNum, Integer stack) {
//        return "R" + rowNum + "C" + colNum + "S" + stack;
//    }
//
//    private ToolResponseDTO convertToToolResponseDTO(tool tool) {
//        ToolResponseDTO dto = new ToolResponseDTO();
//        dto.setId(tool.getId());
//        dto.setName(tool.getName());
//        dto.setCategoryName(tool.getCategory() != null ? tool.getCategory().getName() : null);
//        dto.setType(tool.getType() != null ? tool.getType().name() : null);
//        dto.setIsExpensive(tool.getIsExpensive());
//        dto.setThreshold(tool.getThreshold());
//        dto.setQuantity(tool.getQty());
//        dto.setImage(tool.getImageUrl());
//
//        if (tool.getStorageArea() != null) {
//            dto.setStorageAreaName(tool.getStorageArea().getBucket());
//            dto.setFactoryName(tool.getStorageArea().getFactory().getName());
//        }
//
//        return dto;
//    }
//
//    private StorageAreaResponseDTO convertToStorageAreaResponseDTO(storageArea storageArea) {
//        StorageAreaResponseDTO dto = new StorageAreaResponseDTO();
//        dto.setId(storageArea.getId());
//        dto.setBucket(storageArea.getBucket());
//        dto.setRowNum(storageArea.getRowNum());
//        dto.setColNum(storageArea.getColNum());
//        dto.setStack(storageArea.getStack());
//        dto.setFactoryName(storageArea.getFactory() != null ? storageArea.getFactory().getName() : null);
//        return dto;
//    }
//}