package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.Specifications.UserSpecifications;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.entity.userFactory;
import com.example.inventory_factory_management.entity.users_centralOffice;
import com.example.inventory_factory_management.repository.UserCentralOfficeRepository;
import com.example.inventory_factory_management.repository.userFactoryRepository;
import com.example.inventory_factory_management.repository.userRepository;
import com.example.inventory_factory_management.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class userService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserCentralOfficeRepository userCentralOfficeRepository;

    @Autowired
    private userFactoryRepository userFactoryRepository;

    @Autowired
    private SecurityUtil securityUtil;



    // Add this method to handle profile image upload
    public BaseResponseDTO<UserDTO> updateProfile(Long userId, UserUpdateDTO userDTO, MultipartFile profileImage) {        try {
            user user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user fields
            if (userDTO.getUsername() != null) {
                user.setUsername(userDTO.getUsername());
            }
            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }
            if (userDTO.getPhone() != null) {
                user.setPhone(Long.parseLong(userDTO.getPhone()));
            }

            // Handle profile image upload
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(profileImage);
                user.setImg(imageUrl);
            } else if (userDTO.getImg() != null && !userDTO.getImg().trim().isEmpty()) {
                // Keep existing image if no new image provided
                user.setImg(userDTO.getImg());
            }

            user.setUpdatedAt(LocalDateTime.now());
            user updatedUser = userRepository.save(user);

            return BaseResponseDTO.success("Profile updated successfully", convertToDTO(updatedUser));
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update profile: " + e.getMessage());
        }
    }



    public BaseResponseDTO<UserProfileWithFactoryDTO> getUserProfile() {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtil.getCurrentUser();

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
            // Only OWNER can access this method (enforced by @PreAuthorize)
            user requestedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserProfileWithFactoryDTO profileDTO = convertToProfileWithFactoriesDTO(requestedUser);
            return BaseResponseDTO.success(profileDTO);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get user profile: " + e.getMessage());
        }
    }

    // New conversion method for profile with factories
    private UserProfileWithFactoryDTO convertToProfileWithFactoriesDTO(user user) {
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

    private UserProfileDTO convertToProfileDTO(user user) {
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


    private List<FactoryInfoDTO> getFactoryInfoForUser(user userEntity) {
        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();

        switch (userEntity.getRole()) {
            case CENTRAL_OFFICER:
                // Central officers belong to central office
                Optional<users_centralOffice> centralMappings = userCentralOfficeRepository.findByUser(userEntity);
                if (centralMappings.isPresent()) {
                    FactoryInfoDTO centralOfficeInfo = new FactoryInfoDTO();
                    centralOfficeInfo.setFactoryId(null); // No factory ID for central office
                    centralOfficeInfo.setFactoryName("Central Office");
                    centralOfficeInfo.setLocation(centralMappings.get().getOffice().getLocation());
                    factoryInfo.add(centralOfficeInfo);
                }
                break;

            case MANAGER:
                // Managers can belong to multiple factories
                List<userFactory> managerMappings = userFactoryRepository.findByUser(userEntity);
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
                List<userFactory> workerMappings = userFactoryRepository.findByUser(userEntity);
                if (!workerMappings.isEmpty()) {
                    userFactory mapping = workerMappings.get(0);
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

    private UserDTO convertToDTO(user user) {
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