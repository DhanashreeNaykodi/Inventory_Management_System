package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import com.example.inventory_factory_management.repository.FactoryRepository;
import com.example.inventory_factory_management.repository.UserFactoryRepository;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.utils.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FactoryService {

    @Autowired
    FactoryRepository factoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserFactoryRepository userFactoryRepository;

    @Autowired
    private EmailService emailService;


    public BaseResponseDTO<CountResponseDTO> getFactoriesCount() {
        try {
            long count = factoryRepository.count();
            return BaseResponseDTO.success(CountResponseDTO.of(count, "factories"));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factory count: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Page<FactoryDTO>> getAllFactories(BaseRequestDTO request) {
        try {
//            Pageable pageable = createPageable(request);
            Pageable pageable = PaginationUtil.toPageable(request);
            Page<Factory> factoryPage = factoryRepository.findAll(pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);
            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve factories: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Page<FactoryDTO>> getFactoriesByCity(String city, BaseRequestDTO request) {
        try {
//            Pageable pageable = createPageable(request);
            Pageable pageable = PaginationUtil.toPageable(request);
            Page<Factory> factoryPage = factoryRepository.findByCityAndStatus(city, AccountStatus.ACTIVE, pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);

            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to filter factories: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<FactoryDTO>> searchFactoriesByName(String name, BaseRequestDTO request) {
        try {
//            Pageable pageable = createPageable(request);
            Pageable pageable = PaginationUtil.toPageable(request);
            Page<Factory> factoryPage = factoryRepository.findByNameContainingIgnoreCase(name, pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);

            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search factories: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Page<UserDTO>> getAllManagers(BaseRequestDTO request) {
        try {
//            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Pageable pageable = PaginationUtil.toPageable(request);

            Page<User> managerPage = userRepository.findByRole(Role.MANAGER, pageable);
            Page<UserDTO> dtoPage = managerPage.map(this::convertUserToDTO);

            return BaseResponseDTO.success("Managers retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get managers: " + e.getMessage());
        }
    }


//    public BaseResponseDTO<FactoryDTO> createFactory(FactoryDTO factoryDTO) {
//        try {
//            if (factoryDTO.getName() == null || factoryDTO.getName().trim().isEmpty()) {
//                return BaseResponseDTO.error("Factory name is required");
//            }
//
//            // FIX: Check for plantHead instead of manager
//            if (factoryDTO.getPlantHead() == null && factoryDTO.getPlantHeadId() == null) {
//                return BaseResponseDTO.error("Plant head details are required"); // Updated error message
//            }
//
//            if (factoryRepository.existsByName(factoryDTO.getName())) {
//                return BaseResponseDTO.error("Factory with name '" + factoryDTO.getName() + "' already exists");
//            }
//
//            user plantHead;
//            String generatedPassword = null;
//
//            if (factoryDTO.getPlantHeadId() != null) {
//                // Use existing plant head
//                plantHead = userRepository.findById(factoryDTO.getPlantHeadId())
//                        .orElseThrow(() -> new RuntimeException("Plant head not found with id: " + factoryDTO.getPlantHeadId()));
//
//                if (plantHead.getRole() != Role.MANAGER) {
//                    return BaseResponseDTO.error("User with id " + factoryDTO.getPlantHeadId() + " is not a manager");
//                }
//            } else {
//                // Create new plant head from plantHead object
//                UserDTO plantHeadDetails = factoryDTO.getPlantHead();
//
//                if (userRepository.findByEmail(plantHeadDetails.getEmail()).isPresent()) {
//                    return BaseResponseDTO.error("User with email '" + plantHeadDetails.getEmail() + "' already exists");
//                }
//
//                plantHead = new user();
//                plantHead.setUsername(plantHeadDetails.getUsername());
//                plantHead.setEmail(plantHeadDetails.getEmail());
//                plantHead.setPhone(Long.parseLong(plantHeadDetails.getPhone()));
//                plantHead.setRole(Role.MANAGER);
//                plantHead.setStatus(account_status.ACTIVE);
//
//                generatedPassword = "12345";
//                plantHead.setPassword(passwordEncoder.encode(generatedPassword));
//                plantHead = userRepository.save(plantHead);
//            }
//
//            // Create factory entity
//            factory newFactory = new factory();
//            newFactory.setName(factoryDTO.getName());
//            newFactory.setCity(factoryDTO.getCity());
//            newFactory.setAddress(factoryDTO.getAddress());
//            newFactory.setPlantHead(plantHead);
//            newFactory.setStatus(account_status.ACTIVE);
//            newFactory.setCreatedAt(LocalDateTime.now());
//            newFactory.setUpdatedAt(LocalDateTime.now());
//
//            factory savedFactory = factoryRepository.save(newFactory);
//
//            // Create userFactory relationship
//            userFactory userFactoryRelation = new userFactory();
//            userFactoryRelation.setUser(plantHead);
//            userFactoryRelation.setFactory(savedFactory);
//            userFactoryRelation.setUserRole(Role.MANAGER);
//            userFactoryRepository.save(userFactoryRelation);
//
//            FactoryDTO responseDTO = convertToDTO(savedFactory);
//            return BaseResponseDTO.success("Factory created successfully", responseDTO);
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to create factory: " + e.getMessage());
//        }
//    }

    public BaseResponseDTO<FactoryDTO> createFactory(FactoryDTO factoryDTO) {
        try {
            if (factoryDTO.getName() == null || factoryDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Factory name is required");
            }

            if (factoryDTO.getPlantHead() == null && factoryDTO.getPlantHeadId() == null) {
                return BaseResponseDTO.error("Plant head details are required");
            }

            if (factoryRepository.existsByName(factoryDTO.getName())) {
                return BaseResponseDTO.error("Factory with name '" + factoryDTO.getName() + "' already exists");
            }

            User plantHead;
            String generatedPassword = null;
            boolean isNewManager = false;

            if (factoryDTO.getPlantHeadId() != null) {
                plantHead = userRepository.findById(factoryDTO.getPlantHeadId())
                        .orElseThrow(() -> new RuntimeException("Plant head not found with id: " + factoryDTO.getPlantHeadId()));

                if (plantHead.getRole() != Role.MANAGER) {
                    return BaseResponseDTO.error("User with id " + factoryDTO.getPlantHeadId() + " is not a manager");
                }

                boolean isManagerAssigned = userFactoryRepository.existsByUserAndUserRoleAndStatus(
                        plantHead, Role.MANAGER, AccountStatus.ACTIVE);

                if (isManagerAssigned) {
                    return BaseResponseDTO.error("Manager is already assigned to another factory and cannot be reassigned");
                }
            } else {
                UserDTO plantHeadDetails = factoryDTO.getPlantHead();
                if (userRepository.findByEmail(plantHeadDetails.getEmail()).isPresent()) {
                    return BaseResponseDTO.error("User with email '" + plantHeadDetails.getEmail() + "' already exists");
                }

                plantHead = new User();
                plantHead.setUsername(plantHeadDetails.getUsername());
                plantHead.setEmail(plantHeadDetails.getEmail());
                plantHead.setPhone(Long.parseLong(plantHeadDetails.getPhone()));
                plantHead.setRole(Role.MANAGER);
                plantHead.setStatus(AccountStatus.ACTIVE);
                plantHead.setImg("src/main/resources/static/images/user-profile-icon.jpg");

                generatedPassword = plantHead.getUsername().substring(0, 3) + "@" + plantHead.getPhone().toString().substring(0,7);
                plantHead.setPassword(passwordEncoder.encode(generatedPassword));
                plantHead = userRepository.save(plantHead);
                isNewManager = true; //new
            }

            // Create factory entity
            Factory newFactory = new Factory();
            newFactory.setName(factoryDTO.getName());
            newFactory.setCity(factoryDTO.getCity());
            newFactory.setAddress(factoryDTO.getAddress());
            newFactory.setPlantHead(plantHead);
            newFactory.setStatus(AccountStatus.ACTIVE);
            newFactory.setCreatedAt(LocalDateTime.now());
            newFactory.setUpdatedAt(LocalDateTime.now());

            Factory savedFactory = factoryRepository.save(newFactory);

            // Create userFactory relationship
            UserFactory userFactoryRelation = new UserFactory();
            userFactoryRelation.setUser(plantHead);
            userFactoryRelation.setFactory(savedFactory);
            userFactoryRelation.setUserRole(Role.MANAGER);
            userFactoryRepository.save(userFactoryRelation);

            sendManagerEmail(plantHead, savedFactory.getName(), generatedPassword, isNewManager);

            FactoryDTO responseDTO = convertToDTO(savedFactory);

            // Update success message based on manager type
            String successMessage = isNewManager ?
                    "Factory created successfully and new manager account created. Login details sent via email." :
                    "Factory created successfully and existing manager assigned. Notification sent via email.";

            return BaseResponseDTO.success(successMessage, responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create factory: " + e.getMessage());
        }
    }


    private void sendManagerEmail(User manager, String factoryName, String password, boolean isNewManager) {
        try {
            String subject = "Factory Manager Assignment - Inventory System";
            String message;

            if (isNewManager) {
                message = "Dear " + manager.getUsername() + ",\n\n" +
                        "Welcome to Inventory Factory Management System!\n\n" +
                        "Your manager account has been created successfully.\n" +
                        "You have been assigned as the manager of:\n" +
                        "Factory: " + factoryName + "\n\n" +
                        "Your Login Credentials:\n" +
                        "Email: " + manager.getEmail() + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please login and change your password immediately.\n\n" +
                        "Login URL: http://localhost:8080/auth/login\n\n" +
                        "Best regards,\n" +
                        "Inventory Factory Management Team";
            } else {
                message = "Dear " + manager.getUsername() + ",\n\n" +
                        "You have been assigned as the manager of:\n" +
                        "Factory: " + factoryName + "\n\n" +
                        "Your existing account has been linked to this factory.\n" +
                        "Please use your existing credentials to login.\n\n" +
                        "Login Email: " + manager.getEmail() + "\n" +
                        "Login URL: https://herschel-hyperneurotic-hilma.ngrok-free.dev/auth/login\n\n" +
                        "Best regards,\n" +
                        "Inventory Factory Management Team";
            }

            // Send email
            emailService.sendEmail(manager.getEmail(), subject, message);

        } catch (Exception e) {
            System.err.println("Failed to send email to manager: " + e.getMessage());
        }
    }

    public BaseResponseDTO<FactoryDTO> getFactoryById(Long factoryId) {
        try {
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));
            return BaseResponseDTO.success(convertToDTO(factory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factory: " + e.getMessage());
        }
    }


    @Transactional
    public BaseResponseDTO<FactoryDTO> updateFactoryManager(Long factoryId, ManagerUpdateRequest request) {
        try {
            Factory existingFactory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            if(existingFactory.getStatus() == AccountStatus.INACTIVE) {
                return BaseResponseDTO.error("Cannot assign manager for deleted factory");
            }

            if (request.getManagerId() == null && request.getManagerDetails() == null) {
                return BaseResponseDTO.error("Either manager ID or manager details are required");
            }

            User newManager;
            boolean isNewManager = false;
            String generatedPassword = null;

            if (request.getManagerId() != null) {
                newManager = userRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

                if (newManager.getRole() != Role.MANAGER || newManager.getStatus() == AccountStatus.INACTIVE) {
                    return BaseResponseDTO.error("User is not a manager");
                }

                boolean isManagerAssigned = userFactoryRepository.existsByUserAndUserRoleAndStatus(
                        newManager, Role.MANAGER, AccountStatus.ACTIVE);

                if (isManagerAssigned) {
                    return BaseResponseDTO.error("Manager is already assigned to another factory and cannot be reassigned");
                }
            } else {
                UserDTO managerDetails = request.getManagerDetails();

                if (managerDetails.getEmail() == null || managerDetails.getEmail().trim().isEmpty()) {
                    return BaseResponseDTO.error("Manager email is required");
                }

                if (userRepository.findByEmail(managerDetails.getEmail()).isPresent()) {
                    return BaseResponseDTO.error("User with email '" + managerDetails.getEmail() + "' already exists");
                }

                newManager = new User();
                newManager.setUsername(managerDetails.getUsername());
                newManager.setEmail(managerDetails.getEmail());
                newManager.setPhone(Long.parseLong(managerDetails.getPhone()));
                newManager.setRole(Role.MANAGER);
                newManager.setStatus(AccountStatus.ACTIVE);
                newManager.setImg("src/main/resources/static/images/user-profile-icon.jpg");

                generatedPassword = managerDetails.getUsername().substring(0,3) + "@" + managerDetails.getPhone().substring(0,7);
                newManager.setPassword(passwordEncoder.encode(generatedPassword));
                newManager = userRepository.save(newManager);
                isNewManager = true;
            }

            existingFactory.setPlantHead(newManager);
            existingFactory.setUpdatedAt(LocalDateTime.now());
            Factory updatedFactory = factoryRepository.save(existingFactory);

            Optional<UserFactory> existingManagerRelation = userFactoryRepository.findByFactoryAndUserRole(updatedFactory, Role.MANAGER);
            if (existingManagerRelation.isPresent()) {
                UserFactory oldRelation = existingManagerRelation.get();
                oldRelation.setStatus(AccountStatus.INACTIVE);
                userFactoryRepository.save(oldRelation);
            }

            List<UserFactory> existingUserRelations = userFactoryRepository.findByUserAndStatus(newManager, AccountStatus.ACTIVE);
            for (UserFactory relation : existingUserRelations) {
                relation.setStatus(AccountStatus.INACTIVE);
                userFactoryRepository.save(relation);
            }

            UserFactory newRelation = new UserFactory();
            newRelation.setUser(newManager);
            newRelation.setFactory(updatedFactory);
            newRelation.setUserRole(Role.MANAGER);
            newRelation.setStatus(AccountStatus.ACTIVE);

            sendManagerEmail(newManager, updatedFactory.getName(), generatedPassword, isNewManager);

            return BaseResponseDTO.success("Factory manager updated successfully", convertToDTO(updatedFactory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update factory manager: " + e.getMessage());
        }
    }

    public BaseResponseDTO<String> deleteFactory(Long id) {
        try {
            Factory factory1 = factoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Factory doesn't exist"));

            List<User> users = userRepository.findUsersByFactoryId(id);

            if (!users.isEmpty()) {
                users.stream()
                        .filter(u -> u.getRole() == Role.WORKER || u.getRole() == Role.CHIEF_SUPERVISOR)
                        .forEach(u -> u.setStatus(AccountStatus.INACTIVE));
                userRepository.saveAll(users);
            }

            List<UserFactory> userFactories = userFactoryRepository.findByFactoryId(id);
            if (!userFactories.isEmpty()) {
                userFactories.forEach(uf -> uf.setStatus(AccountStatus.INACTIVE));
                userFactoryRepository.saveAll(userFactories);
            }

            // Update factory status
            factory1.setStatus(AccountStatus.INACTIVE);
            factoryRepository.save(factory1);

            return BaseResponseDTO.success("Factory deleted successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete factory: " + e.getMessage());
        }
    }


    public BaseResponseDTO<String> toggleFactoryStatus(Long id) {
        try {
            Factory factory1 = factoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Factory doesn't exist"));

            // Toggle the factory status
            AccountStatus newStatus = (factory1.getStatus() == AccountStatus.ACTIVE)
                    ? AccountStatus.INACTIVE
                    : AccountStatus.ACTIVE;

            factory1.setStatus(newStatus);
            factoryRepository.save(factory1);

            // If deactivating factory, also deactivate associated users and userFactories
            if (newStatus == AccountStatus.INACTIVE) {
                // Deactivate WORKER and CHIEF_SUPERVISOR users
                List<User> users = userRepository.findUsersByFactoryId(id);
                if (!users.isEmpty()) {
                    users.stream()
                            .filter(u -> u.getRole() == Role.WORKER || u.getRole() == Role.CHIEF_SUPERVISOR)
                            .forEach(u -> u.setStatus(AccountStatus.INACTIVE));
                    userRepository.saveAll(users);
                }

                // Deactivate userFactory relationships
                List<UserFactory> userFactories = userFactoryRepository.findByFactoryId(id);
                if (!userFactories.isEmpty()) {
                    userFactories.forEach(uf -> uf.setStatus(AccountStatus.INACTIVE));
                    userFactoryRepository.saveAll(userFactories);
                }
            }

            String action = (newStatus == AccountStatus.ACTIVE) ? "activated" : "deactivated";
            return BaseResponseDTO.success("Factory " + action + " successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to toggle factory status: " + e.getMessage());
        }
    }


    private FactoryDTO convertToSummaryDTO(Factory factory) {
        FactoryDTO dto = new FactoryDTO();
        dto.setFactoryId(factory.getFactoryId());
        dto.setName(factory.getName());
        dto.setCity(factory.getCity());
        dto.setAddress(factory.getAddress());
        dto.setStatus(factory.getStatus());
        if (factory.getPlantHead() != null) {
            dto.setPlantHead(convertUserToDTO(factory.getPlantHead()));
        }
        return dto;
    }

    private FactoryDTO convertToDTO(Factory factory) {
        FactoryDTO dto = new FactoryDTO();
        dto.setFactoryId(factory.getFactoryId());
        dto.setName(factory.getName());
        dto.setCity(factory.getCity());
        dto.setAddress(factory.getAddress());
        dto.setStatus(factory.getStatus());

        if (factory.getPlantHead() != null) {
            dto.setPlantHead(convertUserToDTO(factory.getPlantHead()));
            dto.setPlantHeadId(factory.getPlantHead().getUserId());
        }

        // Get workers
        if (factory.getUserFactories() != null) {
            List<UserDTO> workers = factory.getUserFactories()
                    .stream()
                    .filter(uf -> uf.getUserRole() == Role.WORKER && uf.getUser() != null)
                    .map(uf -> convertUserToDTO(uf.getUser()))
                    .collect(Collectors.toList());
            dto.setWorkers(workers);
        }

        dto.setCreatedAt(factory.getCreatedAt());
        dto.setUpdatedAt(factory.getUpdatedAt());
        return dto;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImg(user.getImg());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Set factory information if available
        if (user.getUserFactories() != null && !user.getUserFactories().isEmpty()) {

            UserFactory userFactoryRel = user.getUserFactories().get(0);
            if (userFactoryRel.getFactory() != null) {
                dto.setFactoryId(userFactoryRel.getFactory().getFactoryId());
                dto.setFactoryName(userFactoryRel.getFactory().getName());
                dto.setFactoryRole(userFactoryRel.getUserRole());
            }
        }

        return dto;
    }


    public BaseResponseDTO<String> toggleFactoryStatus(String factoryName) {
        try {
            List<Factory> factories = factoryRepository.findByNameContainingIgnoreCase(factoryName);

            if (factories.isEmpty()) {
                return BaseResponseDTO.error("Factory '" + factoryName + "' doesn't exist");
            }

            Factory factory1 = factories.get(0);

            // Toggle the factory status
            AccountStatus newStatus = (factory1.getStatus() == AccountStatus.ACTIVE)
                    ? AccountStatus.INACTIVE
                    : AccountStatus.ACTIVE;

            factory1.setStatus(newStatus);
            factoryRepository.save(factory1);

            // If deactivating factory, also deactivate associated users and userFactories
            if (newStatus == AccountStatus.INACTIVE) {
                // Deactivate WORKER and CHIEF_SUPERVISOR users
                List<User> users = userRepository.findUsersByFactoryId(factory1.getFactoryId());
                if (!users.isEmpty()) {
                    users.stream()
                            .filter(u -> u.getRole() == Role.WORKER || u.getRole() == Role.CHIEF_SUPERVISOR)
                            .forEach(u -> u.setStatus(AccountStatus.INACTIVE));
                    userRepository.saveAll(users);
                }

                // Deactivate userFactory relationships
                List<UserFactory> userFactories = userFactoryRepository.findByFactoryId(factory1.getFactoryId());
                if (!userFactories.isEmpty()) {
                    userFactories.forEach(uf -> uf.setStatus(AccountStatus.INACTIVE));
                    userFactoryRepository.saveAll(userFactories);
                }
            }
            String action = (newStatus == AccountStatus.ACTIVE) ? "activated" : "deactivated";
            return BaseResponseDTO.success("Factory '" + factoryName + "' " + action + " successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to toggle factory status: " + e.getMessage());
        }
    }
    // Helper method to create Pageable with sorting
//    private Pageable createPageable(BaseRequestDTO request) {
//        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
//        return PageRequest.of(request.getPage(), request.getSize(), sort);
//    }
//
//    // Helper method to create Sort
//    private Sort createSort(String sortBy, String sortDirection) {
//        if (sortBy == null || sortBy.trim().isEmpty()) {
//            sortBy = "createdAt"; // default sort field
//        }
//
//        if (sortDirection == null || sortDirection.trim().isEmpty()) {
//            sortDirection = "DESC"; // default sort direction
//        }
//
//        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
//                Sort.Direction.ASC : Sort.Direction.DESC;
//
//        return Sort.by(direction, sortBy);
//    }

}
