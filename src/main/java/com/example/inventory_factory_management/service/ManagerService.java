package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import com.example.inventory_factory_management.exceptions.OperationNotPermittedException;
import com.example.inventory_factory_management.exceptions.ResourceNotFoundException;
import com.example.inventory_factory_management.exceptions.UserAlreadyExistsException;
import com.example.inventory_factory_management.exceptions.UserNotFoundException;
import com.example.inventory_factory_management.repository.UserFactoryRepository;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.specifications.UserSpecifications;
import com.example.inventory_factory_management.utils.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactoryRepository userFactoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmployeeService employeeService; // Reuse existing service

    @Autowired
    private FactoryService factoryService;

    // Create a new manager
    @Transactional
    public BaseResponseDTO<UserDTO> createManager(UserDTO managerDTO) {

            // Check if email already exists
            if (userRepository.findByEmail(managerDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with given email already exists");
            }

            // Create new manager
            User newManager = new User();
            newManager.setUsername(managerDTO.getUsername());
            newManager.setEmail(managerDTO.getEmail());

            if (managerDTO.getPhone() != null && !managerDTO.getPhone().trim().isEmpty()) {
                newManager.setPhone(Long.parseLong(managerDTO.getPhone()));
            }

            newManager.setRole(Role.MANAGER);
            newManager.setStatus(AccountStatus.ACTIVE);

            // Generate password
            String generatedPassword = managerDTO.getUsername().substring(0,3) + "@" + managerDTO.getPhone().substring(0,7);
            newManager.setPassword(passwordEncoder.encode(generatedPassword));

            newManager.setCreatedAt(LocalDateTime.now());
            newManager.setUpdatedAt(LocalDateTime.now());

            User savedManager = userRepository.save(newManager);

            // Send welcome email
            sendManagerWelcomeEmail(savedManager, generatedPassword);

            UserDTO responseDTO = convertToUserDTO(savedManager);
            return BaseResponseDTO.success("Manager created successfully", responseDTO);

    }

//    // Get all managers with pagination and filtering
    public BaseResponseDTO<Page<UserDTO>> getAllManagers(String search, String status, BaseRequestDTO request) {
        try {
            Pageable pageable = PaginationUtil.toPageable(request);
            // Convert string status to enum

            AccountStatus accountStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    accountStatus = AccountStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            // Single method call for all filters
            Specification<User> spec = UserSpecifications.withFilters(
                    search,
                    Role.MANAGER.name(), // role
                    null, // factoryId
                    accountStatus // status
            );

            Page<User> managerPage = userRepository.findAll(spec, pageable);
            Page<UserDTO> dtoPage = managerPage.map(this::convertToUserDTO);

            return BaseResponseDTO.success("Managers retrieved successfully", dtoPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to retrieve managers: " + e.getMessage());
        }
    }

    // Get manager by ID
    public BaseResponseDTO<UserDTO> getManagerById(Long managerId) {
        try {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            if (manager.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("User is not a manager");
//                throw new
            }

            UserDTO responseDTO = convertToUserDTO(manager);
            return BaseResponseDTO.success("Manager retrieved successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get manager: " + e.getMessage());
        }
    }

    // Get manager by name
    public BaseResponseDTO<UserDTO> getManagerByName(String managerName) {
        try {
            User manager = userRepository.findByUsernameAndRole(managerName, Role.MANAGER)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found with name: " + managerName));

            UserDTO responseDTO = convertToUserDTO(manager);
            return BaseResponseDTO.success("Manager retrieved successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get manager: " + e.getMessage());
        }
    }


    public BaseResponseDTO<Page<UserDTO>> searchManagersByName(String managerName, BaseRequestDTO request) {
        try {
            Pageable pageable = PaginationUtil.toPageable(request);

            // Search for managers with username containing the search term
            List<User> managers = userRepository.findByUsernameContainingIgnoreCaseAndRole(managerName, Role.MANAGER);

            Page<UserDTO> dtoPage = new org.springframework.data.domain.PageImpl<>(
                    managers.stream().map(this::convertToUserDTO).collect(Collectors.toList()),
                    pageable,
                    managers.size()
            );

            return BaseResponseDTO.success("Managers search completed successfully", dtoPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search managers: " + e.getMessage());
        }
    }

    // Update manager details
    @Transactional
    public BaseResponseDTO<UserDTO> updateManager(Long managerId, UserUpdateDTO managerDTO, MultipartFile imageFile) {
        try {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            if (manager.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("User is not a manager");
            }

            // Update fields if provided
            if (managerDTO.getUsername() != null && !managerDTO.getUsername().trim().isEmpty()) {
                // Check if new username already exists for another manager
//                Optional<User> existingManager = userRepository.findByUsernameAndRole(managerDTO.getUsername(), Role.MANAGER);
//                if (existingManager.isPresent() && !existingManager.get().getUserId().equals(managerId)) {
//                    return BaseResponseDTO.error("Manager with username '" + managerDTO.getUsername() + "' already exists");
//                }
                manager.setUsername(managerDTO.getUsername());
            }

            if (managerDTO.getEmail() != null && !managerDTO.getEmail().equals(manager.getEmail())) {
                if (userRepository.findByEmail(managerDTO.getEmail()).isPresent()) {
//                    return BaseResponseDTO.error("Email already exists");
                    throw new UserAlreadyExistsException("User email already exists");
                }
                manager.setEmail(managerDTO.getEmail());
            }

            if (managerDTO.getPhone() != null && !managerDTO.getPhone().trim().isEmpty()) {
                manager.setPhone(Long.parseLong(managerDTO.getPhone()));
            }

//            if (managerDTO. != null) {
//                manager.setImg(managerDTO.getImg());
//            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                manager.setImg(imageUrl);
            }

            manager.setUpdatedAt(LocalDateTime.now());
            User updatedManager = userRepository.save(manager);

            UserDTO responseDTO = convertToUserDTO(updatedManager);
            return BaseResponseDTO.success("Manager updated successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update manager: " + e.getMessage());
        }
    }

    // Delete manager (soft delete)
    @Transactional
    public BaseResponseDTO<String> deleteManager(Long managerId) {
        try {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            if (manager.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("User is not a manager");
            }

            // Check if manager is assigned to any active factories
            List<UserFactory> managerFactories = userFactoryRepository.findByUser(manager);
            boolean hasActiveFactories = managerFactories.stream()
                    .anyMatch(uf -> uf.getFactory().getStatus() == AccountStatus.ACTIVE);

            if (hasActiveFactories) {
//                return BaseResponseDTO.error("Cannot delete manager who is assigned to active factories. Please reassign factories first.");
                throw new OperationNotPermittedException("Cannot delete manager who is assigned to active factories. Please reassign factories first.");
            }

            // Soft delete manager
            manager.setStatus(AccountStatus.INACTIVE);
            manager.setUpdatedAt(LocalDateTime.now());
            userRepository.save(manager);

            return BaseResponseDTO.success("Manager deleted successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete manager: " + e.getMessage());
        }
    }

    // Toggle manager status
    @Transactional
    public BaseResponseDTO<String> toggleManagerStatus(Long managerId) {
        try {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            if (manager.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("User is not a manager");
            }

            // Toggle status
            AccountStatus newStatus = (manager.getStatus() == AccountStatus.ACTIVE) ? AccountStatus.INACTIVE
                    : AccountStatus.ACTIVE;

            manager.setStatus(newStatus);
            manager.setUpdatedAt(LocalDateTime.now());
            userRepository.save(manager);

            String action = (newStatus == AccountStatus.ACTIVE) ? "activated" : "deactivated";
            return BaseResponseDTO.success("Manager " + action + " successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to toggle manager status: " + e.getMessage());
        }
    }

    // Get available managers (without factory assignment or with inactive factories only)
    public BaseResponseDTO<Page<UserDTO>> getAvailableManagers(BaseRequestDTO request) {
        try {
            Pageable pageable = PaginationUtil.toPageable(request);

            Page<User> availableManagers = userRepository.findAll(
                    UserSpecifications.availableManagers(), pageable);

            Page<UserDTO> dtoPage = availableManagers.map(this::convertToUserDTO);

            return BaseResponseDTO.success("Available managers retrieved successfully", dtoPage);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get available managers: " + e.getMessage());
        }
    }




    private void sendManagerWelcomeEmail(User manager, String password) {
        try {
            String subject = "Welcome to Inventory Factory Management System - Manager Account Created";
            String message = "Dear " + manager.getUsername() + ",\n\n" +
                    "Your manager account has been created successfully.\n\n" +
                    "Your Login Credentials:\n" +
                    "Email: " + manager.getEmail() + "\n" +
                    "Password: " + password + "\n\n" +
                    "Please login and change your password immediately.\n\n" +
                    "Login URL: http://localhost:8080/auth/login\n\n" +
                    "Best regards,\n" +
                    "Inventory Factory Management Team";

            emailService.sendEmail(manager.getEmail(), subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }


    public BaseResponseDTO<FactoryDTO> getManagerFactory() {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentManager = userRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new UserNotFoundException("Manager not found"));

            Optional<UserFactory> managerFactoryRelation = userFactoryRepository
                    .findByUserAndUserRoleAndStatus(currentManager, Role.MANAGER, AccountStatus.ACTIVE);

            if (managerFactoryRelation.isEmpty()) {
//                return BaseResponseDTO.error("No factory assigned to this manager");
                throw new ResourceNotFoundException("No factory assigned to this manager");
            }

            Factory managerFactory = managerFactoryRelation.get().getFactory();
            FactoryDTO factoryDTO = convertToDetailedDTO(managerFactory);

            return BaseResponseDTO.success("Factory details retrieved successfully", factoryDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factory details: " + e.getMessage());
        }
    }

    private FactoryDTO convertToDetailedDTO(Factory factory) {
        FactoryDTO dto = new FactoryDTO();
        dto.setFactoryId(factory.getFactoryId());
        dto.setName(factory.getName());
        dto.setCity(factory.getCity());
        dto.setAddress(factory.getAddress());
        dto.setStatus(factory.getStatus());

        if (factory.getPlantHead() != null) {
            dto.setPlantHead(convertUserToDTO(factory.getPlantHead()));
        }

        dto.setCreatedAt(factory.getCreatedAt());
        dto.setUpdatedAt(factory.getUpdatedAt());
        return dto;
    }

    // Helper methods
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImg(user.getImg());
        dto.setPhone(user.getPhone() != null ? user.getPhone().toString() : null);
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Set factory information if available
        List<UserFactory> userFactories = userFactoryRepository.findByUser(user);
        if (!userFactories.isEmpty()) {
            // Get the first active factory assignment
            UserFactory userFactoryRel = userFactories.stream()
                    .filter(uf -> uf.getFactory().getStatus() == AccountStatus.ACTIVE)
                    .findFirst()
                    .orElse(userFactories.get(0));

            if (userFactoryRel.getFactory() != null) {
                dto.setFactoryId(userFactoryRel.getFactory().getFactoryId());
                dto.setFactoryName(userFactoryRel.getFactory().getName());
                dto.setFactoryRole(userFactoryRel.getUserRole());
            }
        }

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
        return dto;
    }

}