package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.exceptions.InvalidActionException;
import com.example.inventory_factory_management.exceptions.ResourceAlreadyExistsException;
import com.example.inventory_factory_management.exceptions.ResourceNotFoundException;
import com.example.inventory_factory_management.exceptions.UnauthorizedAccessException;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.specifications.ToolSpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ToolService {

    @Autowired
    private final ToolRepository toolRepository;
    @Autowired
    private final ToolCategoryRepository toolCategoryRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private final ToolStockRepository toolStockRepository;
    @Autowired
    private final SecurityUtil securityUtils;
    @Autowired
    private FactoryRepository factoryRepository;
    @Autowired
    private final StorageAreaRepository storageAreaRepository;
    @Autowired
    private final ToolStorageMappingRepository toolStorageMappingRepository;


    public BaseResponseDTO<ToolResponseDTO> createTool(CreateToolDTO createToolDTO) {
            if (toolRepository.existsByNameIgnoreCase(createToolDTO.getName())) {
                throw new ResourceAlreadyExistsException("Tool with this name already exists");
            }
            ToolCategory category = toolCategoryRepository.findById(createToolDTO.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            Tool tool = new Tool();
            tool.setName(createToolDTO.getName());
            tool.setCategory(category);
            tool.setType(createToolDTO.getType());
            tool.setIsExpensive(createToolDTO.getIsExpensive());
            tool.setThreshold(createToolDTO.getThreshold());

            if (createToolDTO.getImage() != null && !createToolDTO.getImage().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(createToolDTO.getImage());
                    tool.setImageUrl(imageUrl);
                } catch (Exception e) {
                    tool.setImageUrl("src/main/resources/static/images/user-profile-icon.jpg");
                }
            } else {
                tool.setImageUrl("src/main/resources/static/images/user-profile-icon.jpg");
            }
            tool.setUpdatedAt(LocalDateTime.now());
            tool.setStatus(AccountStatus.ACTIVE);
            Tool savedTool = toolRepository.save(tool);
            return BaseResponseDTO.success("Tool created successfully", convertToDTO(savedTool));

    }

    public BaseResponseDTO<Page<ToolResponseDTO>> getAllTools(BaseRequestDTO request, Long categoryId, AccountStatus status, ToolType type, Expensive isExpensive, String search) {

            if (request == null) {
                request = new BaseRequestDTO();
            }
            Specification<Tool> spec = ToolSpecifications.withFilters(categoryId, status, type, isExpensive, search);

            Pageable pageable = PaginationUtil.toPageable(request);

            Page<Tool> toolPage = toolRepository.findAll(spec, pageable);
            Page<ToolResponseDTO> toolDTOsPage = toolPage.map(this::convertToDTO);

            return BaseResponseDTO.success("Tools retrieved successfully", toolDTOsPage);

    }

    public BaseResponseDTO<ToolResponseDTO> getToolById(Long id) {
            Tool tool = toolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
            return BaseResponseDTO.success("Tool retrieved successfully", convertToDTO(tool));
    }

    public BaseResponseDTO<ToolResponseDTO> updateTool(Long id, CreateToolDTO updateToolDTO) {
            Tool tool = toolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));

            if (!tool.getName().equals(updateToolDTO.getName()) && toolRepository.existsByNameIgnoreCase(updateToolDTO.getName())) {
                throw new ResourceAlreadyExistsException("Tool with this name already exists");
            }
            if (!tool.getCategory().getId().equals(updateToolDTO.getCategoryId())) {

                ToolCategory category = toolCategoryRepository.findById(updateToolDTO.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                tool.setCategory(category);
            }

            if (updateToolDTO.getImage() != null && !updateToolDTO.getImage().isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(updateToolDTO.getImage());
                tool.setImageUrl(imageUrl);
            }

            tool.setName(updateToolDTO.getName());
            tool.setType(updateToolDTO.getType());
            tool.setIsExpensive(updateToolDTO.getIsExpensive());
            tool.setThreshold(updateToolDTO.getThreshold());
            tool.setUpdatedAt(LocalDateTime.now());
            Tool updatedTool = toolRepository.save(tool);

            return BaseResponseDTO.success("Tool updated successfully", convertToDTO(updatedTool));

    }

    public BaseResponseDTO<String> deleteTool(Long id) {
            Tool tool = toolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
//            tool.setStatus(AccountStatus.INACTIVE);
            toolRepository.delete(tool);
            return BaseResponseDTO.success("Tool deleted successfully", "Tool with ID " + id + " deleted");
    }

    private ToolResponseDTO convertToDTO(Tool tool) {
        ToolResponseDTO dto = new ToolResponseDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setCategoryId(tool.getCategory().getId());
        dto.setCategoryName(tool.getCategory().getName());
        dto.setType(tool.getType().name());
        dto.setIsExpensive(tool.getIsExpensive());
        dto.setThreshold(tool.getThreshold());
        dto.setImageUrl(tool.getImageUrl());
        dto.setStatus(tool.getStatus());
        dto.setCreatedAt(tool.getCreatedAt());
        return dto;
    }



//    MANAGERS - ADDING TOOLS INTO STOCK

//    @Transactional
    public BaseResponseDTO<String> addToolsToFactoryStock(AssignToolToFactoryDTO requestDTO) {
            User currentUser = securityUtils.getCurrentUser();

            if (!currentUser.getRole().name().equals("MANAGER")) {
                throw new UnauthorizedAccessException("Only MANAGER can add tools to factory stock");
            }
            Factory factory = currentUser.getUserFactories().stream().findFirst().map(UserFactory::getFactory).orElse(null);

            if (factory == null) {
                throw new ResourceNotFoundException("Factory not found for current user");
            }

            for (int i = 0; i < requestDTO.getTool_ids().size(); i++) {
                Long toolId = requestDTO.getTool_ids().get(i);
                Integer quantity = requestDTO.getQuantities().get(i);
                String storageLocation = requestDTO.getStorage_locations().get(i);
                if (!toolRepository.existsById(toolId)) {
                    throw new ResourceNotFoundException("Tool not found with ID: " + toolId);
                }
                if (quantity <= 0) {
                    throw new InvalidActionException("Quantity must be positive for tool ID: " + toolId);
                }
                String fullLocationCode = "F" + factory.getFactoryId() + "_" + storageLocation;

                Optional<StorageArea> storageArea = storageAreaRepository.findByLocationCode(fullLocationCode);
                if (storageArea.isEmpty()) {
                    throw new ResourceNotFoundException("Storage location not found: " + storageLocation);
                }
            }

            // Add tools to factory stock with storage locations
            for (int i = 0; i < requestDTO.getTool_ids().size(); i++) {
                Long toolId = requestDTO.getTool_ids().get(i);
                Integer quantity = requestDTO.getQuantities().get(i);
                String storageLocation = requestDTO.getStorage_locations().get(i);

                Tool tool = toolRepository.findById(toolId).orElseThrow(() -> new ResourceNotFoundException("Tool not found with ID: " + toolId));

                String fullLocationCode = "F" + factory.getFactoryId() + "_" + storageLocation;
                StorageArea storageArea = storageAreaRepository.findByLocationCode(fullLocationCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Storage location not found: " + storageLocation));

                // Add to factory stock
//                addToolToFactoryStock
                Optional<ToolStock> existingStock = toolStockRepository.findByTool_IdAndFactory_FactoryId(tool.getId(), factory.getFactoryId());
                if (existingStock.isPresent()) {
                    ToolStock stock = existingStock.get();
                    stock.setTotalQuantity(stock.getTotalQuantity() + quantity);
                    stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
                    stock.setUpdatedAt(LocalDateTime.now());
                    toolStockRepository.save(stock);
                    System.out.println("Updated existing stock for tool: " + tool.getName() + ", new quantity: " + stock.getTotalQuantity());
                } else {
                    ToolStock newStock = new ToolStock();
                    newStock.setFactory(factory);
                    newStock.setTool(tool);
                    newStock.setTotalQuantity(quantity.longValue());
                    newStock.setAvailableQuantity(quantity.longValue());
                    newStock.setCreatedAt(LocalDateTime.now());
                    newStock.setUpdatedAt(LocalDateTime.now());
                    toolStockRepository.save(newStock);
                    System.out.println("Created new stock for tool: " + tool.getName() + ", quantity: " + quantity);
                }


                // Create storage mapping
//                addToolToStorageLocation
                // Checking if tool already exists in this storage location
                Optional<ToolStorageMapping> existingMapping = toolStorageMappingRepository.findByFactoryFactoryIdAndToolIdAndStorageAreaLocationCode(factory.getFactoryId(), tool.getId(), storageArea.getLocationCode());

                if (existingMapping.isPresent()) {
                    // Update existing mapping quantity
                    ToolStorageMapping mapping = existingMapping.get();
                    mapping.setQuantity(mapping.getQuantity() + quantity);
                    toolStorageMappingRepository.save(mapping);
                } else {
                    // Create new storage mapping
                    ToolStorageMapping newMapping = new ToolStorageMapping();
                    newMapping.setFactory(factory);
                    newMapping.setTool(tool);
                    newMapping.setStorageArea(storageArea);
                    newMapping.setQuantity(quantity);
                    newMapping.setCreatedAt(LocalDateTime.now());
                    toolStorageMappingRepository.save(newMapping);
                }
            }

            return BaseResponseDTO.success("Tools added to factory stock successfully", "Added " + requestDTO.getTool_ids().size() + " tool(s) to factory stock with storage locations");
    }



    public BaseResponseDTO<Page<ToolStockResponseDTO>> getMyFactoryTools(BaseRequestDTO request) {
            User currentUser = securityUtils.getCurrentUser();

            Factory factory = currentUser.getUserFactories().stream().findFirst().map(UserFactory::getFactory).orElse(null);

            if (factory == null) {
                throw new ResourceNotFoundException("Factory not found for current user");
            }
            Pageable pageable = PaginationUtil.toPageable(request);
            Page<ToolStock> toolStockPage = toolStockRepository.findByFactoryFactoryId(factory.getFactoryId(), pageable);
            Page<ToolStockResponseDTO> toolStockDTOsPage = toolStockPage.map(this::convertToToolStockDTO);
            return BaseResponseDTO.success("Factory tools retrieved successfully", toolStockDTOsPage);

    }

    public BaseResponseDTO<ToolStorageDetailDTO> getToolStorageDetails(Long toolId) {
            User currentUser = securityUtils.getCurrentUser();

            Factory factory = currentUser.getUserFactories().stream().findFirst().map(UserFactory::getFactory).orElse(null);

            if (factory == null) {
                throw new UnauthorizedAccessException("Factory not found for current user");
            }

            // Get tool stock
            ToolStock toolStock = toolStockRepository.findByTool_IdAndFactory_FactoryId(toolId, factory.getFactoryId()).orElseThrow(() -> new ResourceNotFoundException("Tool not found in factory stock"));

            ToolStorageDetailDTO dto = new ToolStorageDetailDTO();
            dto.setToolId(toolStock.getTool().getId());
            dto.setToolName(toolStock.getTool().getName());
            dto.setToolCategory(toolStock.getTool().getCategory().getName());
            dto.setToolType(toolStock.getTool().getType().name());
            dto.setImageUrl(toolStock.getTool().getImageUrl());
            dto.setTotalQuantityInFactory(toolStock.getTotalQuantity());
            dto.setAvailableQuantity(toolStock.getAvailableQuantity());

            List<ToolStorageMapping> storageMappings = toolStorageMappingRepository.findByToolIdAndFactoryFactoryId(toolId, factory.getFactoryId());

            List<ToolStorageDetailDTO.StorageLocationDetail> locationDetails = storageMappings.stream()
                    .map(mapping -> new ToolStorageDetailDTO.StorageLocationDetail(
                            mapping.getStorageArea().getLocationCode(),
                            mapping.getStorageArea().getRowNum(),
                            mapping.getStorageArea().getColNum(),
                            mapping.getStorageArea().getStack(),
                            mapping.getStorageArea().getBucket(),
                            mapping.getQuantity()
                    ))
                    .collect(Collectors.toList());

            dto.setStorageLocations(locationDetails);
            return BaseResponseDTO.success("Tool storage details retrieved successfully", dto);
    }


    // method for paginated location codes
    public BaseResponseDTO<Page<String>> getStorageLocationCodes(BaseRequestDTO request) {

            User currentUser = securityUtils.getCurrentUser();
            Factory factory = currentUser.getUserFactories().stream().findFirst().map(UserFactory::getFactory).orElse(null);

            if (factory == null) {
                throw new ResourceNotFoundException("Factory not found for current user");
            }

            Pageable pageable = PageRequest.of(request.getPage() != null ? request.getPage() : 0, request.getSize() != null ? request.getSize() : 70);
            Page<StorageArea> storageAreaPage = storageAreaRepository.findByFactoryFactoryId(factory.getFactoryId(), pageable);

            Page<String> locationCodesPage = storageAreaPage.map(storageArea -> {String fullCode = storageArea.getLocationCode();
                return fullCode.replace("F" + factory.getFactoryId() + "_", "");
            });
            return BaseResponseDTO.success("Storage location codes retrieved successfully", locationCodesPage);

    }


    private ToolStockResponseDTO convertToToolStockDTO(ToolStock toolStock) {
        ToolStockResponseDTO dto = new ToolStockResponseDTO();
        dto.setId(toolStock.getId());
        dto.setToolId(toolStock.getTool().getId());
        dto.setToolName(toolStock.getTool().getName());
        dto.setToolCategory(toolStock.getTool().getCategory().getName());
        dto.setToolType(toolStock.getTool().getType().name());
        dto.setIsExpensive(toolStock.getTool().getIsExpensive());
        dto.setImageUrl(toolStock.getTool().getImageUrl());
        dto.setTotalQuantity(toolStock.getTotalQuantity());
        dto.setAvailableQuantity(toolStock.getAvailableQuantity());
        dto.setIssuedQuantity(toolStock.getIssuedQuantity());
        dto.setLastUpdatedAt(toolStock.getLastUpdatedAt());
        return dto;
    }
}