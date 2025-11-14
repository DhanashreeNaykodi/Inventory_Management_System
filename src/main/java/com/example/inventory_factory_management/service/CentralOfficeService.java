package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.CentralOffice;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserCentralOffice;
import com.example.inventory_factory_management.repository.CentralOfficeRepository;
import com.example.inventory_factory_management.repository.UserCentralOfficeRepository;
import com.example.inventory_factory_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CentralOfficeService {

    @Autowired
    private CentralOfficeRepository centralOfficeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCentralOfficeRepository userCentralOfficeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


//    CREATING CENTRAL OFFICE WITH HEAD CENTRAL OFFICER
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
            CentralOffice office = new CentralOffice();
            office.setLocation(dto.getLocation() != null ? dto.getLocation() : "Headquarters");
            CentralOffice savedOffice = centralOfficeRepository.save(office);

            // Handle user (central officer head) - FIXED: Proper Optional handling
            Optional<User> existingUser = userRepository.findByEmail(dto.getCentralOfficerHeadEmail());
            User userEntity;

            if (existingUser.isPresent()) {
                userEntity = existingUser.get();
                if (userEntity.getRole() != Role.CENTRAL_OFFICER) {
                    return BaseResponseDTO.error("User already exists with different role.");
                }
            } else {
                // Create new CENTRAL_OFFICER user
                userEntity = new User();
                userEntity.setEmail(dto.getCentralOfficerHeadEmail());
                userEntity.setUsername(dto.getCentralOfficerHeadName() != null ? dto.getCentralOfficerHeadName() : dto.getCentralOfficerHeadEmail());
                userEntity.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "default123"));
                userEntity.setRole(Role.CENTRAL_OFFICER);
                userEntity.setStatus(AccountStatus.ACTIVE);
                userEntity.setCreatedAt(LocalDateTime.now());
                userEntity.setUpdatedAt(LocalDateTime.now());
                userEntity.setImg("src/main/resources/static/images/user-profile-icon.jpg");
                userEntity = userRepository.save(userEntity);

                sendCentralOfficerWelcomeEmail(userEntity, dto.getPassword() != null ? dto.getPassword() : "default123");
            }

            // Check if mapping already exists
            if (userCentralOfficeRepository.existsByUser(userEntity)) {
                return BaseResponseDTO.error("Central Officer is already assigned to an office");
            }

            // Map this officer to the central office
            UserCentralOffice mapping = new UserCentralOffice();
            mapping.setOffice(savedOffice);
            mapping.setUser(userEntity);
            userCentralOfficeRepository.save(mapping);

            return BaseResponseDTO.success("Central Office created successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create central office: " + e.getMessage());
        }
    }

    
//    INDIVIDUAL CHIEF OFFICERS
    @Transactional
    public BaseResponseDTO<Void> addChiefOfficerToOffice(AddChiefOfficerDTO dto) {
        try {
            // Step 1: Check if the Central Office exists
            CentralOffice office = centralOfficeRepository.findById(dto.getCentralOfficeId())
                    .orElseThrow(() -> new RuntimeException("Central Office not found with ID: " + dto.getCentralOfficeId()));

            // Step 2: Check if user with email already exists
            Optional<User> existingUserOpt = userRepository.findByEmail(dto.getCentralOfficerEmail());

            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();

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
                //Create new user only if email doesn't exist
                // Validate email doesn't exist in any role
                if (userRepository.existsByEmail(dto.getCentralOfficerEmail())) {
                    return BaseResponseDTO.error("User with email '" + dto.getCentralOfficerEmail() + "' already exists");
                }

                User newUser = new User();
                newUser.setEmail(dto.getCentralOfficerEmail());
                newUser.setUsername(dto.getCentralOfficerName() != null ? dto.getCentralOfficerName() : dto.getCentralOfficerEmail());
                newUser.setPassword(passwordEncoder.encode("default123"));
                newUser.setRole(Role.CENTRAL_OFFICER);
                newUser.setStatus(AccountStatus.ACTIVE);

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

            // Get the user (either existing or newly created)
            User officer = userRepository.findByEmail(dto.getCentralOfficerEmail())
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve user after creation"));

            // Check if mapping already exists for this office
            if (userCentralOfficeRepository.existsByUserAndOffice(officer, office)) {
                return BaseResponseDTO.error("Chief Officer is already assigned to this office");
            }

            // Map the officer to the Central Office
            UserCentralOffice mapping = new UserCentralOffice();
            mapping.setOffice(office);
            mapping.setUser(officer);
            userCentralOfficeRepository.save(mapping);

            return BaseResponseDTO.success("Central Officer added to Central Office successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to add central officer: cannot create chief officer with same email" + e.getMessage());
        }
    }

    // Updated email method name to match new naming convention
    private void sendCentralOfficerWelcomeEmail(User centralOfficer, String password) {
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


    public BaseResponseDTO<List<CentralOfficeResponseDTO>> getCentralOfficers() {
        try {
            List<CentralOffice> offices = centralOfficeRepository.findAll();

            // Convert Entity → DTO
            List<CentralOfficeResponseDTO> officeDtos = offices.stream().map(office -> {
                CentralOfficeResponseDTO dto = new CentralOfficeResponseDTO();
                dto.setId(office.getCentralOfficeId());
                dto.setLocation(office.getLocation());

                // Map all users (officers) related to this office
                if (office.getUserMappings() != null && !office.getUserMappings().isEmpty()) {
                    List<UserProfileDTO> officers = office.getUserMappings().stream()
                            .map(mapping -> {
                                User user = mapping.getUser();
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
            User chiefOfficer = userRepository.findById(chiefOfficerId)
                    .orElseThrow(() -> new RuntimeException("Chief Officer not found"));

            // Find the mapping
            Optional<UserCentralOffice> mapping = userCentralOfficeRepository.findByUser(chiefOfficer);
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
            List<User> centralOfficers = userRepository.findByUsernameContainingIgnoreCaseAndRole(chiefOfficerName, Role.CENTRAL_OFFICER);

            if (centralOfficers.isEmpty()) {
                return BaseResponseDTO.error("No chief officer found with name: " + chiefOfficerName);
            }

            // If multiple officers found with similar names, return list for disambiguation
            if (centralOfficers.size() > 1) {
                List<String> officerNames = centralOfficers.stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList());
                return BaseResponseDTO.error("Multiple chief officers found. Please be more specific: " + officerNames);
            }

            User chiefOfficer = centralOfficers.get(0);

            // Find the mapping
            Optional<UserCentralOffice> mapping = userCentralOfficeRepository.findByUser(chiefOfficer);
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
            List<User> centralOfficers = userRepository.findByUsernameContainingIgnoreCaseAndRole(name, Role.CENTRAL_OFFICER);

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

    private CentralOfficerDTO convertToCentralOfficerDTO(User officer) {
        CentralOfficerDTO dto = new CentralOfficerDTO();
        dto.setUserId(officer.getUserId());
        dto.setName(officer.getUsername()); // This is correct - mapping username to name in DTO
        dto.setEmail(officer.getEmail());
        dto.setPhone(officer.getPhone() != null ? officer.getPhone().toString() : null);
        dto.setStatus(officer.getStatus().toString());

        // Get office information
        Optional<UserCentralOffice> mapping = userCentralOfficeRepository.findByUser(officer);
        if (mapping.isPresent()) {
            dto.setOfficeId(mapping.get().getOffice().getCentralOfficeId());
            dto.setOfficeName(mapping.get().getOffice().getLocation()+ " office");
        }

        return dto;
    }

}