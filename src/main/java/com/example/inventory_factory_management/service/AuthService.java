package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.exceptions.InvalidCredentialsException;
import com.example.inventory_factory_management.exceptions.ResourceAlreadyExistsException;
import com.example.inventory_factory_management.exceptions.UserNotFoundException;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.security.JwtAuth;
import com.example.inventory_factory_management.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtAuth jwtAuth;
    private TokenBlacklistService tokenBlacklistService;
    private SecurityUtil securityUtil;
    private EmailService emailService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtAuth jwtAuth, TokenBlacklistService tokenBlacklistService, SecurityUtil securityUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuth = jwtAuth;
        this.tokenBlacklistService = tokenBlacklistService;
        this.securityUtil = securityUtil;
        this.emailService = emailService;
    }




    public LoginResponseDTO loginService(LoginDTO logindto) {
        User user = userRepository.findByEmail(logindto.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(passwordEncoder.matches(logindto.getPassword(), user.getPassword())) {

            String token = jwtAuth.generateToken(user);
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setRole(user.getRole());

            return response;
        }

        throw new InvalidCredentialsException("Invalid password");
    }


    public BaseResponseDTO<UserDTO> registerDistributor(RegisterDistributorDTO registerDTO) {

        // Check if user email already exists
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already registered: " + registerDTO.getName() + "\n Login to your account.");
        }
        if(userRepository.findByPhone(registerDTO.getPhone()).isPresent()) {
            throw new ResourceAlreadyExistsException("Contact number already registered " + registerDTO.getPhone());
        }

        // Create new distributor user
        User distributor = new User();
        distributor.setUsername(registerDTO.getName());
        distributor.setEmail(registerDTO.getEmail());
        distributor.setPhone(registerDTO.getPhone());

        String generatedPassword = registerDTO.getName().substring(0,3) + "@" + registerDTO.getPhone().toString().substring(0,7);
        distributor.setPassword(passwordEncoder.encode(generatedPassword));

        distributor.setRole(Role.DISTRIBUTOR);
        distributor.setStatus(AccountStatus.ACTIVE);
        distributor.setCreatedAt(LocalDateTime.now());
        distributor.setUpdatedAt(LocalDateTime.now());

        User savedDistributor = userRepository.save(distributor);
        securityUtil.sendWelcomeEmail(savedDistributor, generatedPassword);

        UserDTO responseDTO = convertDistributorToDTO(savedDistributor);
        securityUtil.sendWelcomeEmail(savedDistributor, generatedPassword);
        return BaseResponseDTO.success("You are successfully registered.\n Check your email for further credentials.", responseDTO);

    }


    private UserDTO convertDistributorToDTO(User user) {
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

        // For distributors, no factory information is set
        dto.setFactoryId(null);
        dto.setFactoryName(null);
        dto.setFactoryRole(null);

        return dto;
    }



    public BaseResponseDTO<String> logout(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            tokenBlacklistService.blacklistToken(token);
            return BaseResponseDTO.success("Logged out successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to logout: " + e.getMessage());
        }
    }
}
