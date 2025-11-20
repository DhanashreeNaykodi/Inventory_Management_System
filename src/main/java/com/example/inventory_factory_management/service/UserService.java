package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import com.example.inventory_factory_management.entity.UserCentralOffice;
import com.example.inventory_factory_management.exceptions.OperationNotPermittedException;
import com.example.inventory_factory_management.exceptions.UserNotFoundException;
import com.example.inventory_factory_management.repository.UserCentralOfficeRepository;
import com.example.inventory_factory_management.repository.UserFactoryRepository;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserCentralOfficeRepository userCentralOfficeRepository;

    @Autowired
    private UserFactoryRepository userFactoryRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    PasswordEncoder passwordEncoder;


    // handle update profile
    @Transactional
    public BaseResponseDTO<UserDTO> updateProfile(Long userId, UserUpdateDTO userDTO, MultipartFile profileImage) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
            boolean updated = false;

            if (userDTO.getUsername() != null) {
                user.setUsername(userDTO.getUsername());
                updated = true;
            }

            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
                updated = true;
            }

            if (userDTO.getPhone() != null) {
                try {
                    user.setPhone(Long.parseLong(userDTO.getPhone()));
                    updated = true;
                } catch (NumberFormatException e) {
                    throw new OperationNotPermittedException("Invalid phone number");
                }
            }

            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(profileImage);
                user.setImg(imageUrl);
                updated = true;
            }

            if (!updated) {
                throw new OperationNotPermittedException("At least one field required to update profile");
            }
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            return BaseResponseDTO.success("Profile updated successfully", convertToDTO(updatedUser));

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update profile: " + e.getMessage());
        }
    }



    public BaseResponseDTO<UserProfileWithFactoryDTO> getUserProfile() {
        try {
            User currentUser = securityUtil.getCurrentUser();

            // Convert to DTO with factories and return
            UserProfileWithFactoryDTO profileDTO = convertToProfileWithFactoriesDTO(currentUser);
            return BaseResponseDTO.success(profileDTO);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get user profile: " + e.getMessage());
        }
    }


    // For OWNER only - get any user's profile with factories
    public BaseResponseDTO<UserProfileWithFactoryDTO> getUserProfileById(Long userId) {
        try {
            User requestedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            UserProfileWithFactoryDTO profileDTO = convertToProfileWithFactoriesDTO(requestedUser);
            return BaseResponseDTO.success(profileDTO);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get user profile: " + e.getMessage());
        }
    }

    private UserProfileWithFactoryDTO convertToProfileWithFactoriesDTO(User user) {
        UserProfileWithFactoryDTO dto = new UserProfileWithFactoryDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImg(user.getImg());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());

        // Add factory information based on role
        List<FactoryInfoDTO> factoryInfo = getFactoryInfoForUser(user);
        dto.setFactories(factoryInfo);

        return dto;
    }

    private UserProfileDTO convertToProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImg(user.getImg());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }


    private List<FactoryInfoDTO> getFactoryInfoForUser(User userEntity) {
        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();

        switch (userEntity.getRole()) {
            case CENTRAL_OFFICER:
                // Central officers belong to central office
                Optional<UserCentralOffice> centralMappings = userCentralOfficeRepository.findByUser(userEntity);
                if (centralMappings.isPresent()) {
                    FactoryInfoDTO centralOfficeInfo = new FactoryInfoDTO();
                    centralOfficeInfo.setFactoryId(null); // No factory ID for central office
                    centralOfficeInfo.setFactoryName("Central Office");
                    centralOfficeInfo.setLocation(centralMappings.get().getOffice().getLocation());
                    factoryInfo.add(centralOfficeInfo);
                }
                break;

            case MANAGER:
                // Managers belong to a factory
                List<UserFactory> managerMappings = userFactoryRepository.findByUser(userEntity);
                factoryInfo.addAll(managerMappings.stream()
                        .map(mapping -> {
                            FactoryInfoDTO info = new FactoryInfoDTO();
                            info.setFactoryId(mapping.getFactory().getFactoryId());
                            info.setFactoryName(mapping.getFactory().getName());
                            info.setLocation(mapping.getFactory().getCity());
                            return info;
                        })
                        .collect(Collectors.toList()));
                break;

            case CHIEF_SUPERVISOR:
            case WORKER:
                // Supervisors and workers belong to one factory
                List<UserFactory> workerMappings = userFactoryRepository.findByUser(userEntity);
                if (!workerMappings.isEmpty()) {
                    UserFactory mapping = workerMappings.get(0);
                    FactoryInfoDTO info = new FactoryInfoDTO();
                    info.setFactoryId(mapping.getFactory().getFactoryId());
                    info.setFactoryName(mapping.getFactory().getName());
                    info.setLocation(mapping.getFactory().getCity());
                    factoryInfo.add(info);
                }
                break;
        }

        return factoryInfo;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setImg(user.getImg());
        dto.setPhone(user.getPhone().toString()); // Convert Long to String
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Add factory information if available
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
}