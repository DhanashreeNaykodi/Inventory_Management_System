package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.RequestStatus;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductRestockRequestService {

    private CentralOfficeProductRequestRepository centralOfficeProductRequestRepository;
    private UserRepository userRepository;
    private FactoryRepository factoryRepository;
    private ProductRepository productRepository;
    private CentralOfficeRepository centralOfficeRepository;
    private FactoryProductProductionRepository factoryProductProductionRepository;
    private FactoryProductInventoryRepository factoryProductInventoryRepository;
    private EmailService emailService;
    private SecurityUtil securityUtil;
    private UserFactoryRepository userFactoryRepository;

    @Autowired
    public ProductRestockRequestService(CentralOfficeProductRequestRepository centralOfficeProductRequestRepository, UserRepository userRepository, FactoryRepository factoryRepository, ProductRepository productRepository, CentralOfficeRepository centralOfficeRepository, FactoryProductProductionRepository factoryProductProductionRepository, FactoryProductInventoryRepository factoryProductInventoryRepository, EmailService emailService, SecurityUtil securityUtil, UserFactoryRepository userFactoryRepository) {
        this.centralOfficeProductRequestRepository = centralOfficeProductRequestRepository;
        this.userRepository = userRepository;
        this.factoryRepository = factoryRepository;
        this.productRepository = productRepository;
        this.centralOfficeRepository = centralOfficeRepository;
        this.factoryProductProductionRepository = factoryProductProductionRepository;
        this.factoryProductInventoryRepository = factoryProductInventoryRepository;
        this.emailService = emailService;
        this.securityUtil = securityUtil;
        this.userFactoryRepository = userFactoryRepository;
    }


    // Helper method to create Pageable with sorting
    private Pageable createPageable(BaseRequestDTO request) {
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    // Helper method to create Sort
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "requestedAt"; // default sort field
        }

        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "DESC"; // default sort direction
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }



    // Chief Officer creates restock request
    public BaseResponseDTO<RestockRequestDTO> createRestockRequest(CreateRestockRequestDTO requestDTO) {
        try {
            // Get current authenticated user using SecurityUtil
            User currentUser = securityUtil.getCurrentUser();

            if (currentUser.getRole() != Role.CENTRAL_OFFICER) {
                return BaseResponseDTO.error("Only central officers can create restock requests");
            }

            // Validate factory by name
            Factory factory = factoryRepository.findByName(requestDTO.getFactoryName())
                    .orElseThrow(() -> new RuntimeException("Factory not found with name: " + requestDTO.getFactoryName()));

            // Validate product
            Product product = productRepository.findById(requestDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Validate quantity
            if (requestDTO.getQtyRequested() == null || requestDTO.getQtyRequested() <= 0) {
                return BaseResponseDTO.error("Quantity must be greater than 0");
            }

            // Create restock request
            CentralOfficeProductRequest restockRequest = new CentralOfficeProductRequest();
            restockRequest.setFactory(factory);
            restockRequest.setProduct(product);
            restockRequest.setQtyRequested(requestDTO.getQtyRequested());
            restockRequest.setStatus(RequestStatus.PENDING);
            restockRequest.setCreatedAt(LocalDateTime.now());
            restockRequest.setRequestedBy(currentUser);

            CentralOfficeProductRequest savedRequest = centralOfficeProductRequestRepository.save(restockRequest);

            return BaseResponseDTO.success("Restock request created successfully", convertToDTO(savedRequest));

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create restock request: " + e.getMessage());
        }
    }



    // Factory manager can update stock without request
    public BaseResponseDTO<String> updateStockDirectly(UpdateStockDTO stockDTO) {
        try {
            // Get current authenticated user using SecurityUtil
            User currentUser = securityUtil.getCurrentUser();

            if (currentUser.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("Only factory managers can update stock");
            }

            // Get manager's assigned factory automatically
            Long userFactoryId = securityUtil.getCurrentUserFactoryId();
            if (userFactoryId == null) {
                return BaseResponseDTO.error("Manager is not assigned to any factory");
            }

            Factory factory = factoryRepository.findById(userFactoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            // Validate product
            Product product = productRepository.findById(stockDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Validate quantity
            if (stockDTO.getQuantity() == null || stockDTO.getQuantity() <= 0) {
                return BaseResponseDTO.error("Quantity must be greater than 0");
            }

            // Update production and inventory
            updateProductionAndInventory(factory, product, stockDTO.getQuantity(), currentUser);
            return BaseResponseDTO.success("Stock updated successfully for your " + factory.getName());

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update stock: " + e.getMessage());
        }
    }

    // Get all restock requests with pagination - all chief officers
    public BaseResponseDTO<List<RestockRequestDTO>> getAllRestockRequests(RequestStatus status, BaseRequestDTO request) {
        try {
            Pageable pageable = createPageable(request);
            Page<CentralOfficeProductRequest> restockPage;

            if (status != null) {
                // Filter by specific status
                restockPage = centralOfficeProductRequestRepository.findByStatus(status, pageable);
            } else {
                // Get all requests if no status specified
                restockPage = centralOfficeProductRequestRepository.findAll(pageable);
            }

            List<RestockRequestDTO> content = restockPage.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            BaseResponseDTO<List<RestockRequestDTO>> response = BaseResponseDTO.success(
                    status != null ?
                            status + " restock requests retrieved successfully" :
                            "All restock requests retrieved successfully",
                    content
            );

            // Add pagination info
            response.setPagination(new BaseResponseDTO.PaginationInfo(
                    restockPage.getNumber(),
                    restockPage.getSize(),
                    restockPage.getTotalElements(),
                    restockPage.getTotalPages(),
                    restockPage.isFirst(),
                    restockPage.isLast()
            ));

            return response;
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve restock requests: " + e.getMessage());
        }
    }

    // Get my restock requests with pagination (for current chief officer)
    public BaseResponseDTO<List<RestockRequestDTO>> getMyRestockRequests(RequestStatus status, BaseRequestDTO request) {
        try {
            User currentUser = securityUtil.getCurrentUser();

            if (currentUser.getRole() != Role.CENTRAL_OFFICER) {
                return BaseResponseDTO.error("Only chief officers can view their requests");
            }

            Pageable pageable = createPageable(request);
            Page<CentralOfficeProductRequest> restockPage;

            if (status != null) {
                // Filter by specific status for current user
                restockPage = centralOfficeProductRequestRepository.findByRequestedByUserIdAndStatus(currentUser.getUserId(), status, pageable);
            } else {
                // Get all requests for current user if no status specified
                restockPage = centralOfficeProductRequestRepository.findByRequestedByUserId(currentUser.getUserId(), pageable);
            }

            List<RestockRequestDTO> content = restockPage.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            BaseResponseDTO<List<RestockRequestDTO>> response = BaseResponseDTO.success(
                    status != null ?
                            "Your " + status + " restock requests retrieved successfully" :
                            "Your restock requests retrieved successfully",
                    content
            );

            // Add pagination info
            response.setPagination(new BaseResponseDTO.PaginationInfo(
                    restockPage.getNumber(),
                    restockPage.getSize(),
                    restockPage.getTotalElements(),
                    restockPage.getTotalPages(),
                    restockPage.isFirst(),
                    restockPage.isLast()
            ));

            return response;
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve your restock requests: " + e.getMessage());
        }
    }

    // Get restock requests by factory name with pagination - not needed i think
//    public BaseResponseDTO<List<RestockRequestDTO>> getRestockRequestsByFactory(String factoryName, RequestStatus status, BaseRequestDTO request) {
//        try {
//            Pageable pageable = createPageable(request);
//            Page<CentralOfficeProductRequest> restockPage;
//
//            if (status != null) {
//                // Filter by factory name and specific status
//                restockPage = centralOfficeProductRequestRepository.findByFactoryNameAndStatus(factoryName, status, pageable);
//            } else {
//                // Get all requests for factory if no status specified
//                restockPage = centralOfficeProductRequestRepository.findByFactoryName(factoryName, pageable);
//            }
//
//            List<RestockRequestDTO> content = restockPage.getContent().stream()
//                    .map(this::convertToDTO)
//                    .collect(Collectors.toList());
//
//            BaseResponseDTO<List<RestockRequestDTO>> response = BaseResponseDTO.success(
//                    status != null ?
//                            status + " restock requests for factory " + factoryName + " retrieved successfully" :
//                            "All restock requests for factory " + factoryName + " retrieved successfully",
//                    content
//            );
//
//            // Add pagination info
//            response.setPagination(new BaseResponseDTO.PaginationInfo(
//                    restockPage.getNumber(),
//                    restockPage.getSize(),
//                    restockPage.getTotalElements(),
//                    restockPage.getTotalPages(),
//                    restockPage.isFirst(),
//                    restockPage.isLast()
//            ));
//
//            return response;
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to retrieve restock requests: " + e.getMessage());
//        }
//    }

    // Get restock requests for manager's assigned factory with pagination
    public BaseResponseDTO<List<RestockRequestDTO>> getMyFactoryRestockRequests(RequestStatus status, BaseRequestDTO request) {
        try {
            if (!securityUtil.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can view factory requests");
            }

            // For managers, automatically filter by their assigned factory
            Long userFactoryId = securityUtil.getCurrentUserFactoryId();
            if (userFactoryId != null) {
                Pageable pageable = createPageable(request);
                Page<CentralOfficeProductRequest> restockPage;

                if (status != null) {
                    // Filter by manager's factory and specific status
                    restockPage = centralOfficeProductRequestRepository.findByFactoryIdAndStatus(userFactoryId, status, pageable);
                } else {
                    // Get all requests for manager's factory if no status specified
                    restockPage = centralOfficeProductRequestRepository.findByFactoryId(userFactoryId, pageable);
                }

                List<RestockRequestDTO> content = restockPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

                BaseResponseDTO<List<RestockRequestDTO>> response = BaseResponseDTO.success(
                        status != null ?
                                status + " restock requests for your factory retrieved successfully" :
                                "All restock requests for your factory retrieved successfully",
                        content
                );

                // Add pagination info
                response.setPagination(new BaseResponseDTO.PaginationInfo(
                        restockPage.getNumber(),
                        restockPage.getSize(),
                        restockPage.getTotalElements(),
                        restockPage.getTotalPages(),
                        restockPage.isFirst(),
                        restockPage.isLast()
                ));

                return response;
            }

            return BaseResponseDTO.error("No factory assigned to your account");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve factory restock requests: " + e.getMessage());
        }
    }

    // Get pending requests for a factory (no pagination - for dropdowns)
//    public BaseResponseDTO<List<RestockRequestDTO>> getPendingRequestsForFactory(String factoryName) {
//        try {
//            // Find factory by name first
//            Factory factory = factoryRepository.findByName(factoryName)
//                    .orElseThrow(() -> new RuntimeException("Factory not found with name: " + factoryName));
//
//            // Check if user has access to this factory
//            if (!securityUtil.hasAccessToFactory(factory.getFactoryId())) {
//                return BaseResponseDTO.error("You don't have access to view requests for this factory");
//            }
//
//            List<CentralOfficeProductRequest> pendingRequests = centralOfficeProductRequestRepository.findPendingRequestsForFactory(factory.getFactoryId());
//            List<RestockRequestDTO> dtoList = pendingRequests.stream()
//                    .map(this::convertToDTO)
//                    .collect(Collectors.toList());
//
//            return BaseResponseDTO.success("Pending restock requests retrieved successfully", dtoList);
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to retrieve pending restock requests: " + e.getMessage());
//        }
//    }

    // Helper methods for production and inventory updates
    private void updateProductionAndInventory(CentralOfficeProductRequest request) {
        updateProductionAndInventory(request.getFactory(), request.getProduct(),
                request.getQtyRequested(), request.getRequestedBy());
    }

    private void updateProductionAndInventory(Factory factory, Product product, Integer quantity, User addedBy) {
        updateFactoryProduction(factory, product, quantity);
        updateFactoryInventory(factory, product, quantity, addedBy);
    }

    private void updateFactoryProduction(Factory factory, Product product, Integer quantity) {
        FactoryProductProduction production = factoryProductProductionRepository
                .findByFactoryAndProduct(factory, product)
                .orElse(new FactoryProductProduction());

        if (production.getId() == null) {
            production.setFactory(factory);
            production.setProduct(product);
            production.setProducedQty(quantity.longValue());
        } else {
            production.setProducedQty(production.getProducedQty() + quantity);
        }

        factoryProductProductionRepository.save(production);
    }

    private void updateFactoryInventory(Factory factory, Product product, Integer quantity, User addedBy) {
        FactoryProductInventory inventory = factoryProductInventoryRepository
                .findByFactoryAndProduct(factory, product)
                .orElse(new FactoryProductInventory());

        if (inventory.getStockEntryId() == null) {
            inventory.setFactory(factory);
            inventory.setProduct(product);
            inventory.setQty(quantity.longValue());
            inventory.setAddedBy(addedBy);
            // No lastUpdated field in FactoryProductInventory - removed
        } else {
            inventory.setQty(inventory.getQty() + quantity);
            // No lastUpdated field in FactoryProductInventory - removed
        }

        factoryProductInventoryRepository.save(inventory);
    }

    private User getFactoryManager(Factory factory) {
        try {
            System.out.println("=== DEBUG: Getting manager for factory ===");
            System.out.println("Factory ID: " + factory.getFactoryId());
            System.out.println("Factory Name: " + factory.getName());

            // Check if factory has plantHead
            if (factory.getPlantHead() != null) {
                System.out.println("Factory has plantHead: " + factory.getPlantHead().getUsername());
                return factory.getPlantHead();
            } else {
                System.out.println("Factory has NO plantHead");
            }

            // Try different repository methods
            System.out.println("--- Trying findByFactoryId ---");
            List<UserFactory> factoryUsersById = userFactoryRepository.findByFactoryId(factory.getFactoryId());
            System.out.println("Found " + factoryUsersById.size() + " users by factory ID");

            // Also try findByFactory if it exists
            try {
                System.out.println("--- Trying findByFactory ---");
                List<UserFactory> factoryUsersByEntity = userFactoryRepository.findByFactory(factory);
                System.out.println("Found " + factoryUsersByEntity.size() + " users by factory entity");
                factoryUsersById.addAll(factoryUsersByEntity);
            } catch (Exception e) {
                System.out.println("findByFactory method not available: " + e.getMessage());
            }

            // Log ALL users in this factory regardless of role
            for (UserFactory uf : factoryUsersById) {
                User user = uf.getUser();
                System.out.println("User in factory: " + user.getUsername() +
                        " (ID: " + user.getUserId() +
                        "), Role: " + user.getRole() +
                        ", UserRole in mapping: " + uf.getUserRole() +
                        ", Status: " + uf.getStatus());
            }

            // Check for managers
            List<UserFactory> managers = factoryUsersById.stream()
                    .filter(uf -> {
                        boolean isManager = uf.getUserRole() == Role.MANAGER;
                        boolean isActive = uf.getStatus() == AccountStatus.ACTIVE;
                        System.out.println("Checking user: " + uf.getUser().getUsername() +
                                ", isManager: " + isManager +
                                ", isActive: " + isActive);
                        return isManager && isActive;
                    })
                    .collect(Collectors.toList());

            System.out.println("Found " + managers.size() + " active managers");

            if (!managers.isEmpty()) {
                User manager = managers.get(0).getUser();
                System.out.println("✅ Using manager: " + manager.getUsername());
                return manager;
            }

            System.out.println("❌ No active manager found");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error getting factory manager: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Update the convertToDTO method to include factory manager info
    private RestockRequestDTO convertToDTO(CentralOfficeProductRequest request) {
        RestockRequestDTO dto = new RestockRequestDTO();
        dto.setId(request.getId());
        dto.setFactoryId(request.getFactory().getFactoryId());
        dto.setFactoryName(request.getFactory().getName());
        dto.setProductId(request.getProduct().getId());
        dto.setProductName(request.getProduct().getName());
        dto.setQtyRequested(request.getQtyRequested());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getCreatedAt());
        dto.setRequestedByUserId(request.getRequestedBy().getUserId());
        dto.setRequestedByUserName(request.getRequestedBy().getUsername());

        // NEW: Get factory manager information
        User factoryManager = getFactoryManager(request.getFactory());
        if (factoryManager != null) {
            dto.setManagerUserId(factoryManager.getUserId());
            dto.setManagerUserName(factoryManager.getUsername());
        }

        // Set completedAt if the request is completed
        if (request.getStatus() == RequestStatus.COMPLETED) {
            dto.setCompletedAt(LocalDateTime.now());
        }

        return dto;
    }


    // Factory manager completes restock request (updated to set completedAt)
    public BaseResponseDTO<RestockRequestDTO> completeRestockRequest(Long requestId) {
        try {
            // Check if user is manager or owner
            if (!securityUtil.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can complete restock requests");
            }

            CentralOfficeProductRequest restockRequest = centralOfficeProductRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Restock request not found"));

            // Check if user has access to this factory
            if (!securityUtil.hasAccessToFactory(restockRequest.getFactory().getFactoryId())) {
                return BaseResponseDTO.error("You don't have access to complete requests for this factory");
            }

            if (restockRequest.getStatus() != RequestStatus.PENDING) {
                return BaseResponseDTO.error("Only pending requests can be completed");
            }

            // Update request status
            restockRequest.setStatus(RequestStatus.COMPLETED);
            CentralOfficeProductRequest updatedRequest = centralOfficeProductRequestRepository.save(restockRequest);

            // Update production and inventory records with the requested quantity
            updateProductionAndInventory(restockRequest);

            return BaseResponseDTO.success("Restock request completed successfully", convertToDTO(updatedRequest));

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to complete restock request: " + e.getMessage());
        }
    }
}
