package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.entity.userFactory;
import com.example.inventory_factory_management.repository.factoryRepository;
import com.example.inventory_factory_management.repository.userFactoryRepository;
import com.example.inventory_factory_management.repository.userRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class factoryService {

    @Autowired
    factoryRepository factoryRepository;

    @Autowired
    userRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    userFactoryRepository userFactoryRepository;

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
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<factory> factoryPage = factoryRepository.findAll(pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);

            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve factories: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<FactoryDTO>> getFactoriesByCity(String city, BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<factory> factoryPage = factoryRepository.findByCityAndStatus(city, account_status.ACTIVE, pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);

            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to filter factories: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<UserDTO>> getAllManagers(BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<user> managerPage = userRepository.findByRole(Role.MANAGER, pageable);
            Page<UserDTO> dtoPage = managerPage.map(this::convertUserToDTO);

            return BaseResponseDTO.success("Managers retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get managers: " + e.getMessage());
        }
    }

    public BaseResponseDTO<Page<FactoryDTO>> searchFactoriesByName(String name, BaseRequestDTO request) {
        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<factory> factoryPage = factoryRepository.findByNameContainingIgnoreCase(name, pageable);
            Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToSummaryDTO);

            return BaseResponseDTO.success("Factories retrieved successfully", dtoPage);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search factories: " + e.getMessage());
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

            // FIX: Check for plantHead instead of manager
            if (factoryDTO.getPlantHead() == null && factoryDTO.getPlantHeadId() == null) {
                return BaseResponseDTO.error("Plant head details are required"); // Updated error message
            }

            if (factoryRepository.existsByName(factoryDTO.getName())) {
                return BaseResponseDTO.error("Factory with name '" + factoryDTO.getName() + "' already exists");
            }

            user plantHead;
            String generatedPassword = null;
            boolean isNewManager = false; // Track if this is a new manager

            if (factoryDTO.getPlantHeadId() != null) {
                // Use existing plant head
                plantHead = userRepository.findById(factoryDTO.getPlantHeadId())
                        .orElseThrow(() -> new RuntimeException("Plant head not found with id: " + factoryDTO.getPlantHeadId()));

                if (plantHead.getRole() != Role.MANAGER) {
                    return BaseResponseDTO.error("User with id " + factoryDTO.getPlantHeadId() + " is not a manager");
                }
            } else {
                // Create new plant head from plantHead object
                UserDTO plantHeadDetails = factoryDTO.getPlantHead();

                if (userRepository.findByEmail(plantHeadDetails.getEmail()).isPresent()) {
                    return BaseResponseDTO.error("User with email '" + plantHeadDetails.getEmail() + "' already exists");
                }

                plantHead = new user();
                plantHead.setUsername(plantHeadDetails.getUsername());
                plantHead.setEmail(plantHeadDetails.getEmail());
                plantHead.setPhone(Long.parseLong(plantHeadDetails.getPhone()));
                plantHead.setRole(Role.MANAGER);
                plantHead.setStatus(account_status.ACTIVE);

                generatedPassword = plantHead.getUsername().substring(0, 3) + "@" + plantHead.getPhone().toString().substring(0,7);
                plantHead.setPassword(passwordEncoder.encode(generatedPassword));
                plantHead = userRepository.save(plantHead);
                isNewManager = true; // This is a new manager
            }

            // Create factory entity
            factory newFactory = new factory();
            newFactory.setName(factoryDTO.getName());
            newFactory.setCity(factoryDTO.getCity());
            newFactory.setAddress(factoryDTO.getAddress());
            newFactory.setPlantHead(plantHead);
            newFactory.setStatus(account_status.ACTIVE);
            newFactory.setCreatedAt(LocalDateTime.now());
            newFactory.setUpdatedAt(LocalDateTime.now());

            factory savedFactory = factoryRepository.save(newFactory);

            // Create userFactory relationship
            userFactory userFactoryRelation = new userFactory();
            userFactoryRelation.setUser(plantHead);
            userFactoryRelation.setFactory(savedFactory);
            userFactoryRelation.setUserRole(Role.MANAGER);
            userFactoryRepository.save(userFactoryRelation);

            // ============ ADD EMAIL FUNCTIONALITY HERE ============
            sendManagerEmail(plantHead, savedFactory.getName(), generatedPassword, isNewManager);
            // ============ END EMAIL FUNCTIONALITY ============

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

    /**
     * ADD THIS METHOD TO YOUR factoryService CLASS
     * Simple email sending for manager notification
     */
    private void sendManagerEmail(user manager, String factoryName, String password, boolean isNewManager) {
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
            // Log email error but don't fail the factory creation
            System.err.println("Failed to send email to manager: " + e.getMessage());
        }
    }

    public BaseResponseDTO<FactoryDTO> getFactoryById(Long factoryId) {
        try {
            factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));
            return BaseResponseDTO.success(convertToDTO(factory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factory: " + e.getMessage());
        }
    }

//    @Transactional
//    public BaseResponseDTO<FactoryDTO> updateFactoryManager(Long factoryId, ManagerUpdateRequest request) {
//        try {
//            factory existingFactory = factoryRepository.findById(factoryId)
//                    .orElseThrow(() -> new RuntimeException("Factory not found"));
//
//            if(existingFactory.getStatus() == account_status.INACTIVE) {
//                return BaseResponseDTO.error("Cannot assign manager for deleted factory");
//            }
//
//            // Validate that either managerId or managerDetails is provided
//            if (request.getManagerId() == null && request.getManagerDetails() == null) {
//                return BaseResponseDTO.error("Either manager ID or manager details are required");
//            }
//
//            user newManager;
//
//            if (request.getManagerId() != null) {
//                // Use existing manager
//                newManager = userRepository.findById(request.getManagerId())
//                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));
//
//                if (newManager.getRole() != Role.MANAGER || newManager.getStatus() == account_status.INACTIVE) {
//                    return BaseResponseDTO.error("User is not a manager");
//                }
//            } else {
//                // Create new manager from managerDetails
//                UserDTO managerDetails = request.getManagerDetails();
//
//                if (managerDetails.getEmail() == null || managerDetails.getEmail().trim().isEmpty()) {
//                    return BaseResponseDTO.error("Manager email is required");
//                }
//
//                if (userRepository.findByEmail(managerDetails.getEmail()).isPresent()) {
//                    return BaseResponseDTO.error("User with email '" + managerDetails.getEmail() + "' already exists");
//                }
//
//                newManager = new user();
//                newManager.setUsername(managerDetails.getUsername());
//                newManager.setEmail(managerDetails.getEmail());
//                newManager.setPhone(Long.parseLong(managerDetails.getPhone()));
//                newManager.setRole(Role.MANAGER);
//                newManager.setStatus(account_status.ACTIVE);
//
//                String generatedPassword = "12345";
//                newManager.setPassword(passwordEncoder.encode(generatedPassword));
//                newManager = userRepository.save(newManager);
//            }
//
//            // Update plant head
//            existingFactory.setPlantHead(newManager);
//            existingFactory.setUpdatedAt(LocalDateTime.now());
//            factoryRepository.save(existingFactory);
//
//            // Update userFactory relationship
//            Optional<userFactory> existingRelation = userFactoryRepository.findByFactoryAndUserRole(existingFactory, Role.MANAGER);
//            if (existingRelation.isPresent()) {
//                userFactory relation = existingRelation.get();
//                relation.setUser(newManager);
//                userFactoryRepository.save(relation);
//            } else {
//                userFactory newRelation = new userFactory();
//                newRelation.setUser(newManager);
//                newRelation.setFactory(existingFactory);
//                newRelation.setUserRole(Role.MANAGER);
//                userFactoryRepository.save(newRelation);
//            }
//
//            return BaseResponseDTO.success("Factory manager updated successfully", convertToDTO(existingFactory));
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to update factory manager: " + e.getMessage());
//        }
//    }

    @Transactional
    public BaseResponseDTO<FactoryDTO> updateFactoryManager(Long factoryId, ManagerUpdateRequest request) {
        try {
            factory existingFactory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            if(existingFactory.getStatus() == account_status.INACTIVE) {
                return BaseResponseDTO.error("Cannot assign manager for deleted factory");
            }

            // Validate that either managerId or managerDetails is provided
            if (request.getManagerId() == null && request.getManagerDetails() == null) {
                return BaseResponseDTO.error("Either manager ID or manager details are required");
            }

            user newManager;
            boolean isNewManager = false;
            String generatedPassword = null;

            if (request.getManagerId() != null) {
                // Use existing manager
                newManager = userRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

                if (newManager.getRole() != Role.MANAGER || newManager.getStatus() == account_status.INACTIVE) {
                    return BaseResponseDTO.error("User is not a manager");
                }
            } else {
                // Create new manager from managerDetails
                UserDTO managerDetails = request.getManagerDetails();

                if (managerDetails.getEmail() == null || managerDetails.getEmail().trim().isEmpty()) {
                    return BaseResponseDTO.error("Manager email is required");
                }

                if (userRepository.findByEmail(managerDetails.getEmail()).isPresent()) {
                    return BaseResponseDTO.error("User with email '" + managerDetails.getEmail() + "' already exists");
                }

                newManager = new user();
                newManager.setUsername(managerDetails.getUsername());
                newManager.setEmail(managerDetails.getEmail());
                newManager.setPhone(Long.parseLong(managerDetails.getPhone()));
                newManager.setRole(Role.MANAGER);
                newManager.setStatus(account_status.ACTIVE);

                generatedPassword = "12345";
                newManager.setPassword(passwordEncoder.encode(generatedPassword));
                newManager = userRepository.save(newManager);
                isNewManager = true;
            }

            // Update plant head
            existingFactory.setPlantHead(newManager);
            existingFactory.setUpdatedAt(LocalDateTime.now());
            factoryRepository.save(existingFactory);

            // Update userFactory relationship
            Optional<userFactory> existingRelation = userFactoryRepository.findByFactoryAndUserRole(existingFactory, Role.MANAGER);
            if (existingRelation.isPresent()) {
                userFactory relation = existingRelation.get();
                relation.setUser(newManager);
                userFactoryRepository.save(relation);
            } else {
                userFactory newRelation = new userFactory();
                newRelation.setUser(newManager);
                newRelation.setFactory(existingFactory);
                newRelation.setUserRole(Role.MANAGER);
                userFactoryRepository.save(newRelation);
            }

            // ============ ADD EMAIL FOR MANAGER UPDATE ============
            sendManagerEmail(newManager, existingFactory.getName(), generatedPassword, isNewManager);
            // ============ END EMAIL FUNCTIONALITY ============

            return BaseResponseDTO.success("Factory manager updated successfully", convertToDTO(existingFactory));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update factory manager: " + e.getMessage());
        }
    }

    public BaseResponseDTO<String> deleteFactory(Long id) {
        try {
            factory factory1 = factoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Factory doesn't exist"));

            // Use the alternative method
            List<user> users = userRepository.findUsersByFactoryId(id);

            if (!users.isEmpty()) {
                users.stream()
                        .filter(u -> u.getRole() == Role.WORKER || u.getRole() == Role.CHIEF_SUPERVISOR)
                        .forEach(u -> u.setStatus(account_status.INACTIVE));
                userRepository.saveAll(users);
            }

            // Use findByFactoryId instead of findByFactory
            List<userFactory> userFactories = userFactoryRepository.findByFactoryId(id);
            if (!userFactories.isEmpty()) {
                userFactories.forEach(uf -> uf.setStatus(account_status.INACTIVE));
                userFactoryRepository.saveAll(userFactories);
            }

            // Update factory status
            factory1.setStatus(account_status.INACTIVE);
            factoryRepository.save(factory1);

            return BaseResponseDTO.success("Factory deleted successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete factory: " + e.getMessage());
        }
    }


    private FactoryDTO convertToSummaryDTO(factory factory) {
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

    private FactoryDTO convertToDTO(factory factory) {
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

    private UserDTO convertUserToDTO(user user) {
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
            // Get the first factory relationship (assuming one manager per factory)
            // get all factories of manager MISSING!!
            userFactory userFactoryRel = user.getUserFactories().get(0);
            if (userFactoryRel.getFactory() != null) {
                dto.setFactoryId(userFactoryRel.getFactory().getFactoryId());
                dto.setFactoryName(userFactoryRel.getFactory().getName());
                dto.setFactoryRole(userFactoryRel.getUserRole());
            }
        }

        return dto;
    }

}
