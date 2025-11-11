package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.centralOffice;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.entity.users_centralOffice;
import com.example.inventory_factory_management.repository.CentralOfficeRepository;
import com.example.inventory_factory_management.repository.UserCentralOfficeRepository;
import com.example.inventory_factory_management.repository.userRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CentralOfficeService {

    @Autowired
    private CentralOfficeRepository centralOfficeRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private UserCentralOfficeRepository userCentralOfficeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public BaseResponseDTO<Void> createCentralOffice(CentralOfficeDTO dto) {
        try {
            // Check if central office already exists
            if (centralOfficeRepository.count() > 0) {
                return BaseResponseDTO.error("A Central Office already exists in the system");
            }

            if (dto.getCentralOfficerHeadEmail() == null || dto.getCentralOfficerHeadEmail().isBlank()) {
                return BaseResponseDTO.error("Central Officer Head email is required");
            }

            // Create central office
            centralOffice office = new centralOffice();
            office.setLocation(dto.getLocation() != null ? dto.getLocation() : "Headquarters");
            centralOffice savedOffice = centralOfficeRepository.save(office);

            // Handle user (central officer head) - FIXED: Proper Optional handling
            Optional<user> existingUser = userRepository.findByEmail(dto.getCentralOfficerHeadEmail());
            user userEntity;

            if (existingUser.isPresent()) {
                userEntity = existingUser.get();
                if (userEntity.getRole() != Role.CENTRAL_OFFICER) {
                    return BaseResponseDTO.error("User exists but is not a Central Officer");
                }
            } else {
                // Create new CENTRAL_OFFICER user
                userEntity = new user();
                userEntity.setEmail(dto.getCentralOfficerHeadEmail());
                userEntity.setUsername(dto.getCentralOfficerHeadName() != null ? dto.getCentralOfficerHeadName() : dto.getCentralOfficerHeadEmail());
                userEntity.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "default123"));
                userEntity.setRole(Role.CENTRAL_OFFICER);
                userEntity.setStatus(account_status.ACTIVE);
                userEntity.setCreatedAt(LocalDateTime.now());
                userEntity.setUpdatedAt(LocalDateTime.now());
                userEntity = userRepository.save(userEntity);

                // Send welcome email only for new users
                sendCentralOfficerWelcomeEmail(userEntity, dto.getPassword() != null ? dto.getPassword() : "default123");
            }

            // Check if mapping already exists
            if (userCentralOfficeRepository.existsByUser(userEntity)) {
                return BaseResponseDTO.error("Central Officer is already assigned to an office");
            }

            // Map this officer to the central office
            users_centralOffice mapping = new users_centralOffice();
            mapping.setOffice(savedOffice);
            mapping.setUser(userEntity);
            userCentralOfficeRepository.save(mapping);

            return BaseResponseDTO.success("Central Office created successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create central office: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDTO<Void> addChiefOfficerToOffice(AddChiefOfficerDTO dto) {
        try {
            // Step 1: Check if the Central Office exists
            centralOffice office = centralOfficeRepository.findById(dto.getCentralOfficeId())
                    .orElseThrow(() -> new RuntimeException("Central Office not found with ID: " + dto.getCentralOfficeId()));

            // Step 2: Check if user with email already exists
            Optional<user> existingUserOpt = userRepository.findByEmail(dto.getCentralOfficerEmail());

            if (existingUserOpt.isPresent()) {
                user existingUser = existingUserOpt.get();

                // Check if user is already a CENTRAL_OFFICER
                if (existingUser.getRole() == Role.CENTRAL_OFFICER) {
                    // Check if already mapped to ANY office
                    if (userCentralOfficeRepository.existsByUser(existingUser)) {
                        return BaseResponseDTO.error("Chief Officer with email '" + dto.getCentralOfficerEmail() + "' already exists and is assigned to an office");
                    }
                    // If CENTRAL_OFFICER but not mapped to any office, proceed to mapping
                } else {
                    // User exists but has different role - check if we should allow this
                    return BaseResponseDTO.error("User with email '" + dto.getCentralOfficerEmail() + "' already exists with role: " + existingUser.getRole());

                }
            } else {
                // Step 3: Create new user only if email doesn't exist
                // Validate email doesn't exist in any role
                if (userRepository.existsByEmail(dto.getCentralOfficerEmail())) {
                    return BaseResponseDTO.error("User with email '" + dto.getCentralOfficerEmail() + "' already exists");
                }

                user newUser = new user();
                newUser.setEmail(dto.getCentralOfficerEmail());
                newUser.setUsername(dto.getCentralOfficerName() != null ? dto.getCentralOfficerName() : dto.getCentralOfficerEmail());
                newUser.setPassword(passwordEncoder.encode("default123"));
                newUser.setRole(Role.CENTRAL_OFFICER);
                newUser.setStatus(account_status.ACTIVE);

                if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
                    try {
                        newUser.setPhone(Long.parseLong(dto.getPhone()));
                    } catch (NumberFormatException e) {
                        return BaseResponseDTO.error("Invalid phone number format");
                    }
                }

                newUser.setCreatedAt(LocalDateTime.now());
                newUser.setUpdatedAt(LocalDateTime.now());
                userRepository.save(newUser);

                // Send welcome email for new user
                sendCentralOfficerWelcomeEmail(newUser, "default123");
            }

            // Step 4: Get the user (either existing or newly created)
            user officer = userRepository.findByEmail(dto.getCentralOfficerEmail())
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve user after creation"));

            // Step 5: Check if mapping already exists for this office
            if (userCentralOfficeRepository.existsByUserAndOffice(officer, office)) {
                return BaseResponseDTO.error("Chief Officer is already assigned to this office");
            }

            // Step 6: Map the officer to the Central Office
            users_centralOffice mapping = new users_centralOffice();
            mapping.setOffice(office);
            mapping.setUser(officer);
            userCentralOfficeRepository.save(mapping);

            return BaseResponseDTO.success("Central Officer added to Central Office successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to add central officer: cannot create chief officer with same email" + e.getMessage());
        }
    }

    // Updated email method name to match new naming convention
    private void sendCentralOfficerWelcomeEmail(user centralOfficer, String password) {
        try {
            String subject = "Welcome Central Officer - Inventory Management System";
            String message = "Dear " + centralOfficer.getUsername() + ",\n\n" +
                    "Welcome to the Inventory Factory Management System as a Central Officer!\n\n" +
                    "Your account has been created successfully.\n\n" +
                    "Your Login Credentials:\n" +
                    "Email: " + centralOfficer.getEmail() + "\n" +
                    "Password: " + password + "\n\n" +
                    "Please login and change your password immediately.\n\n" +
                    "Login URL: http://localhost:8080/auth/login\n\n" +
                    "Best regards,\n" +
                    "Inventory Factory Management Team";

            emailService.sendEmail(centralOfficer.getEmail(), subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }


    public BaseResponseDTO<List<CentralOfficeResponseDTO>> getCentralOffices() {
        try {
            List<centralOffice> offices = centralOfficeRepository.findAll();

            // Convert Entity → DTO
            List<CentralOfficeResponseDTO> officeDtos = offices.stream().map(office -> {
                CentralOfficeResponseDTO dto = new CentralOfficeResponseDTO();
                dto.setId(office.getCentralOfficeId());
                dto.setLocation(office.getLocation());

                // Map all users (officers) related to this office
                if (office.getUserMappings() != null && !office.getUserMappings().isEmpty()) {
                    List<UserProfileDTO> officers = office.getUserMappings().stream()
                            .map(mapping -> {
                                user user = mapping.getUser();
                                UserProfileDTO userDto = new UserProfileDTO();
                                userDto.setUserId(user.getUserId());
                                userDto.setUsername(user.getUsername());
                                userDto.setEmail(user.getEmail());
                                userDto.setRole(user.getRole());
                                userDto.setStatus(user.getStatus());
                                userDto.setImg(user.getImg());
                                userDto.setPhone(user.getPhone() != null ? user.getPhone().toString() : null);
                                return userDto;
                            })
                            .collect(Collectors.toList());
                    dto.setOfficers(officers);
                } else {
                    dto.setOfficers(List.of());
                }

                return dto;
            }).collect(Collectors.toList());

            return BaseResponseDTO.success("Central offices fetched successfully", officeDtos);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get central offices: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponseDTO<Void> removeChiefOfficerFromOffice(Long chiefOfficerId) {
        try {
            // Find the chief officer user
            user chiefOfficer = userRepository.findById(chiefOfficerId)
                    .orElseThrow(() -> new RuntimeException("Chief Officer not found"));

            // Find the mapping
            Optional<users_centralOffice> mapping = userCentralOfficeRepository.findByUser(chiefOfficer);
            if (mapping.isEmpty()) {
                return BaseResponseDTO.error("Chief Officer is not assigned to any office");
            }

            // Remove the mapping
            userCentralOfficeRepository.delete(mapping.get());

            return BaseResponseDTO.success("Chief Officer removed from office successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to remove chief officer: " + e.getMessage());
        }
    }


    @Transactional
    public BaseResponseDTO<Void> removeChiefOfficerByName(String chiefOfficerName) {
        try {
            if (chiefOfficerName == null || chiefOfficerName.trim().isEmpty()) {
                return BaseResponseDTO.error("Chief officer name is required");
            }

            // Search for central officers by name (case-insensitive)
            List<user> centralOfficers = userRepository.findByUsernameContainingIgnoreCaseAndRole(chiefOfficerName, Role.CENTRAL_OFFICER);

            if (centralOfficers.isEmpty()) {
                return BaseResponseDTO.error("No chief officer found with name: " + chiefOfficerName);
            }

            // If multiple officers found with similar names, return list for disambiguation
            if (centralOfficers.size() > 1) {
                List<String> officerNames = centralOfficers.stream()
                        .map(user::getUsername)
                        .collect(Collectors.toList());
                return BaseResponseDTO.error("Multiple chief officers found. Please be more specific: " + officerNames);
            }

            user chiefOfficer = centralOfficers.get(0);

            // Find the mapping
            Optional<users_centralOffice> mapping = userCentralOfficeRepository.findByUser(chiefOfficer);
            if (mapping.isEmpty()) {
                return BaseResponseDTO.error("Chief Officer '" + chiefOfficer.getUsername() + "' is not assigned to any office");
            }

            // Remove the mapping
            userCentralOfficeRepository.delete(mapping.get());

            return BaseResponseDTO.success("Chief Officer '" + chiefOfficer.getUsername() + "' removed from office successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to remove chief officer: " + e.getMessage());
        }
    }



    public BaseResponseDTO<List<CentralOfficerDTO>> searchCentralOfficersByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return BaseResponseDTO.error("Name is required for search");
            }

            // Search for central officers by username (case-insensitive)
            List<user> centralOfficers = userRepository.findByUsernameContainingIgnoreCaseAndRole(name, Role.CENTRAL_OFFICER);

            if (centralOfficers.isEmpty()) {
                return BaseResponseDTO.error("No central officers found with name: " + name);
            }

            // Convert to DTO
            List<CentralOfficerDTO> officerDTOs = centralOfficers.stream()
                    .map(this::convertToCentralOfficerDTO)
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Central officers found successfully", officerDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to search central officers: " + e.getMessage());
        }
    }

    private CentralOfficerDTO convertToCentralOfficerDTO(user officer) {
        CentralOfficerDTO dto = new CentralOfficerDTO();
        dto.setUserId(officer.getUserId());
        dto.setName(officer.getUsername()); // This is correct - mapping username to name in DTO
        dto.setEmail(officer.getEmail());
        dto.setPhone(officer.getPhone() != null ? officer.getPhone().toString() : null);
        dto.setStatus(officer.getStatus().toString());

        // Get office information
        Optional<users_centralOffice> mapping = userCentralOfficeRepository.findByUser(officer);
        if (mapping.isPresent()) {
            dto.setOfficeId(mapping.get().getOffice().getCentralOfficeId());
            dto.setOfficeName(mapping.get().getOffice().getLocation()+ " office");
        }

        return dto;
    }

}