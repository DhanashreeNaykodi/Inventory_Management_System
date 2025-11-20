package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.exceptions.OperationNotPermittedException;
import com.example.inventory_factory_management.exceptions.ResourceNotFoundException;
import com.example.inventory_factory_management.exceptions.UnauthorizedAccessException;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.specifications.CentralOfficeInventorySpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
//@Transactional
@RequiredArgsConstructor
public class ProductRestockRequestService {

    private final CentralOfficeProductRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final FactoryRepository factoryRepo;
    private final ProductRepository productRepo;
    private final FactoryProductProductionRepository productionRepo;
    private final FactoryProductInventoryRepository inventoryRepo;
    private final CentralOfficeInventoryRepository centralInventoryRepo;
    private final SecurityUtil securityUtil;


    //// CHIEF OFFICER :

    // Chief Officer creates restock request
    @Transactional
    public BaseResponseDTO<CentralOfficeRestockResponseDTO> createRestockRequest(CreateRestockRequestDTO requestDTO) {
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser.getRole() != Role.CENTRAL_OFFICER) {
                throw new UnauthorizedAccessException("Only central officers can create restock requests");
            }

            Factory factory = factoryRepo.findById(requestDTO.getFactoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Factory not found with ID: " + requestDTO.getFactoryId()));

            Product product = productRepo.findById(requestDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

//            if (requestDTO.getQtyRequested() == null || requestDTO.getQtyRequested() <= 0) {
//                return BaseResponseDTO.error("Quantity must be greater than 0");
//            }

            // Check if factory has sufficient stock
            Long currentFactoryStock = getCurrentStock(factory, product);

            CentralOfficeProductRequest request = new CentralOfficeProductRequest();
            request.setFactory(factory);
            request.setProduct(product);
            request.setQtyRequested(requestDTO.getQtyRequested());
            request.setStatus(RequestStatus.PENDING);
            request.setCreatedAt(LocalDateTime.now());
            request.setRequestedBy(currentUser);

            CentralOfficeProductRequest savedRequest = requestRepo.save(request);

            CentralOfficeRestockResponseDTO responseDTO = convertToCentralOfficeDTO(savedRequest);
            responseDTO.setCurrentFactoryStock(currentFactoryStock);

            return BaseResponseDTO.success("Restock request created successfully", responseDTO);

    }

    // central office inventory with filters
    public BaseResponseDTO<Page<CentralOfficeInventoryDTO>> getCentralOfficeInventory(Long productId, String productName, Long minQuantity, Long maxQuantity, BaseRequestDTO requestDTO) {

        Specification<CentralOfficeInventory> spec = CentralOfficeInventorySpecifications.withFilters(productId, productName, minQuantity, maxQuantity);

            int page = requestDTO.getPage() == null ? 0 : requestDTO.getPage();
            int size = requestDTO.getSize() == null ? 20 : requestDTO.getSize();
            Pageable pageable = PageRequest.of(page, size);

            Page<CentralOfficeInventoryDTO> resultPage = centralInventoryRepo.findAll(spec, pageable).map(this::convertToInventoryDTO);

            String message = "Central office inventory retrieved successfully";
            return BaseResponseDTO.success(message, resultPage);

    }


    // Chief Officer - Get my restock requests to manager
    public BaseResponseDTO<Page<CentralOfficeRestockResponseDTO>> getMyRestockRequests(RequestStatus status, BaseRequestDTO requestDTO) {

            User currentUser = securityUtil.getCurrentUser();
            if (currentUser.getRole() != Role.CENTRAL_OFFICER) {
//                return BaseResponseDTO.error("Only chief officers can view their requests");
                throw new UnauthorizedAccessException("Only chief officers can view their requests");
            }

            Pageable pageable = PaginationUtil.toPageable(requestDTO);
            Page<CentralOfficeProductRequest> restockPage = (status != null) ? requestRepo.findByRequestedByUserIdAndStatus(currentUser.getUserId(), status, pageable)
                    : requestRepo.findByRequestedByUserId(currentUser.getUserId(), pageable);

            Page<CentralOfficeRestockResponseDTO> resultPage = restockPage.map(this::convertToCentralOfficeDTO);

            String message = status != null ? "Your " + status + " restock requests retrieved successfully"
                    : "Your restock requests retrieved successfully";

            return BaseResponseDTO.success(message, resultPage);
    }



    // Factory manager can update stock when they produce more
    @Transactional
    public BaseResponseDTO<String> updateStockDirectly(UpdateProductStockDTO stockDTO) {

            User currentUser = securityUtil.getCurrentUser();
            if (currentUser.getRole() != Role.MANAGER) {
                throw new UnauthorizedAccessException("Only factory managers can update stock");
            }

            Long factoryId = securityUtil.getCurrentUserFactoryId();
            if (factoryId == null) {
                throw new ResourceNotFoundException("Manager is not assigned to any factory");
            }

            Factory factory = factoryRepo.findById(factoryId).orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

            Product product = productRepo.findById(stockDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if(product.getStatus().equals(AccountStatus.INACTIVE)) {
                throw new OperationNotPermittedException("Inactive products cannot be stocked");
            }

//            if (stockDTO.getQuantity() == null || stockDTO.getQuantity() <= 0) {
//                return BaseResponseDTO.error("Quantity must be greater than 0");
//            }

            // Add to factory inventory and track production
            addToFactoryInventory(factory, product, stockDTO.getQuantity(), currentUser);
            trackFactoryProduction(factory, product, stockDTO.getQuantity());
            return BaseResponseDTO.success(stockDTO.getQuantity() + " units added to " + factory.getName() + " inventory");

    }

    // Factory manager completes restock request
    @Transactional
    public BaseResponseDTO<FactoryRestockResponseDTO> completeRestockRequest(Long requestId) {

            if (!securityUtil.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can complete restock requests");
            }

            CentralOfficeProductRequest request = requestRepo.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Restock request not found"));

            if (!securityUtil.hasAccessToFactory(request.getFactory().getFactoryId())) {
                throw new UnauthorizedAccessException("You don't have access to complete requests for this factory");
            }

            if (request.getStatus() != RequestStatus.PENDING) {
                throw new OperationNotPermittedException("Only pending requests can be completed");
            }

            // Check if factory has enough stock to fulfill the request
            if (!hasSufficientStock(request.getFactory(), request.getProduct(), request.getQtyRequested())) {
                return BaseResponseDTO.error("Insufficient stock. Factory only has " + getCurrentStock(request.getFactory(), request.getProduct()) + " units of " + request.getProduct().getName());
            }

            // Get current factory stock before deduction
            Long currentFactoryStock = getCurrentStock(request.getFactory(), request.getProduct());

            // Deduct from factory inventory (transfer stock out to central office)
            deductFromFactoryInventory(request.getFactory(), request.getProduct(), request.getQtyRequested());

            // Add to central office inventory
            addToCentralOfficeInventory(request.getProduct(), request.getQtyRequested());

            // Update request status to COMPLETED
            request.setStatus(RequestStatus.COMPLETED);
            CentralOfficeProductRequest updatedRequest = requestRepo.save(request);

            // Return factory view
            FactoryRestockResponseDTO responseDTO = convertToFactoryDTO(updatedRequest);
            responseDTO.setCurrentFactoryStock(currentFactoryStock);
            responseDTO.setCompletedAt(LocalDateTime.now());

            return BaseResponseDTO.success("Restock request completed successfully. " + request.getQtyRequested() + " units transferred from " + request.getFactory().getName() + " to central office", responseDTO);

    }

    // Manager - Get all restock requests from CO
    public BaseResponseDTO<Page<FactoryRestockResponseDTO>> getMyFactoryRestockRequests(RequestStatus status, BaseRequestDTO requestDTO) {

            if (!securityUtil.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can view factory requests");
            }
            Long factoryId = securityUtil.getCurrentUserFactoryId();
            if (factoryId == null) {
                throw new ResourceNotFoundException("No factory assigned to your account");
            }
            Pageable pageable = PaginationUtil.toPageable(requestDTO);
            Page<CentralOfficeProductRequest> restockPage = (status != null) ? requestRepo.findByFactory_FactoryIdAndStatus(factoryId, status, pageable) : requestRepo.findByFactory_FactoryId(factoryId, pageable);

            Page<FactoryRestockResponseDTO> resultPage = restockPage.map(this::convertToFactoryDTO);

            String message = status != null ? status + " restock requests for your factory retrieved successfully" : "All restock requests for your factory retrieved successfully";

            return BaseResponseDTO.success(message, resultPage);

    }


    public BaseResponseDTO<Page<FactoryProductCountDTO>> getFactoryWiseProductCounts(BaseRequestDTO request) {

            Pageable pageable = PaginationUtil.toPageable(request, "factoryName");
            Page<Object[]> results = inventoryRepo.getFactoryProductCountsDetailed(pageable);

            // Convert Page<Object[]> to Page<FactoryProductCountDTO>
            Page<FactoryProductCountDTO> resultPage = results.map(result -> new FactoryProductCountDTO((Long) result[0], (String) result[1],
                            (Long) result[2], (String) result[3], (Long) result[4]));

            return BaseResponseDTO.success("Factory product counts retrieved successfully", resultPage);
    }



    // HELPER METHODS
    // Add to central office inventory (accumulate total production from all factories)
    private Long addToCentralOfficeInventory(Product product, Integer quantity) {
        CentralOfficeInventory centralInventory = centralInventoryRepo.findByProduct(product).orElse(new CentralOfficeInventory(product, 0L));

        centralInventory.addQuantity(quantity.longValue());
        CentralOfficeInventory savedInventory = centralInventoryRepo.save(centralInventory);
        return savedInventory.getQuantity();
    }


    // Check if factory has enough stock
    private boolean hasSufficientStock(Factory factory, Product product, Integer requestedQty) {
        Long currentStock = getCurrentStock(factory, product);
        return currentStock >= requestedQty;
    }

    // Get current stock for a product in factory
    private Long getCurrentStock(Factory factory, Product product) {
        return inventoryRepo.findByFactoryAndProduct(factory, product).map(FactoryProductInventory::getQty).orElse(0L);
    }

    // Add to factory inventory (when factory produces more)
    private void addToFactoryInventory(Factory factory, Product product, Integer quantity, User addedBy) {
        FactoryProductInventory inventory = inventoryRepo.findByFactoryAndProduct(factory, product).orElse(new FactoryProductInventory());

        if (inventory.getStockEntryId() == null) {
            inventory.setFactory(factory);
            inventory.setProduct(product);
            inventory.setQty(quantity.longValue());
            inventory.setAddedBy(addedBy);
        } else {
            inventory.setQty(inventory.getQty() + quantity);
        }
        inventoryRepo.save(inventory);
    }

    // Deduct from factory inventory (when fulfilling restock request)
    private void deductFromFactoryInventory(Factory factory, Product product, Integer quantity) {
        FactoryProductInventory inventory = inventoryRepo.findByFactoryAndProduct(factory, product).orElseThrow(() -> new ResourceNotFoundException("Factory inventory not found for " + product.getName()));

        if (inventory.getQty() < quantity) {
            throw new OperationNotPermittedException("Insufficient stock in factory inventory");
        }
        inventory.setQty(inventory.getQty() - quantity);
        inventoryRepo.save(inventory);
    }

    // Track factory production
    private void trackFactoryProduction(Factory factory, Product product, Integer quantity) {
        FactoryProductProduction production = productionRepo.findByFactoryAndProduct(factory, product).orElse(new FactoryProductProduction());

        if (production.getId() == null) {
            production.setFactory(factory);
            production.setProduct(product);
            production.setProducedQty(quantity.longValue());
        } else {
            production.setProducedQty(production.getProducedQty() + quantity);
        }
        productionRepo.save(production);
    }


    private CentralOfficeRestockResponseDTO convertToCentralOfficeDTO(CentralOfficeProductRequest request) {
        CentralOfficeRestockResponseDTO dto = new CentralOfficeRestockResponseDTO();
        dto.setId(request.getId());
        dto.setFactoryId(request.getFactory().getFactoryId());
        dto.setFactoryName(request.getFactory().getName());
        dto.setProductId(request.getProduct().getId());
        dto.setProductName(request.getProduct().getName());
        dto.setQtyRequested(request.getQtyRequested());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());

        // Add current factory stock information
        Long currentStock = getCurrentStock(request.getFactory(), request.getProduct());
        dto.setCurrentFactoryStock(currentStock);

        // Add central office stock information (only for central officers)
        Long centralOfficeStock = centralInventoryRepo.findByProduct(request.getProduct())
                .map(CentralOfficeInventory::getQuantity)
                .orElse(0L);
        dto.setCentralOfficeStock(centralOfficeStock);

        return dto;
    }

    private FactoryRestockResponseDTO convertToFactoryDTO(CentralOfficeProductRequest request) {
        FactoryRestockResponseDTO dto = new FactoryRestockResponseDTO();
        dto.setId(request.getId());
        dto.setFactoryId(request.getFactory().getFactoryId());
        dto.setFactoryName(request.getFactory().getName());
        dto.setProductId(request.getProduct().getId());
        dto.setProductName(request.getProduct().getName());
        dto.setQtyRequested(request.getQtyRequested());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());

        // Add current factory stock information only
        Long currentStock = getCurrentStock(request.getFactory(), request.getProduct());
        dto.setCurrentFactoryStock(currentStock);

        return dto;
    }

    private CentralOfficeInventoryDTO convertToInventoryDTO(CentralOfficeInventory inventory) {
        CentralOfficeInventoryDTO dto = new CentralOfficeInventoryDTO();

        Optional.ofNullable(inventory.getProduct())
                .ifPresent(product -> {
                    dto.setProductId(product.getId());
                    dto.setProductName(product.getName());
                });

        dto.setQuantity(inventory.getQuantity());
        dto.setTotalReceived(inventory.getTotalReceived());
        return dto;
    }
}