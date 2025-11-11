package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.DTO.BaseResponseDTO;
import com.example.inventory_factory_management.DTO.loginDTO;
import com.example.inventory_factory_management.DTO.loginResponseDTO;
import com.example.inventory_factory_management.DTO.signupDto;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.user;
import com.example.inventory_factory_management.repository.userRepository;
import com.example.inventory_factory_management.security.jwtAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class authService {

    private userRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private jwtAuth jwtAuth;
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    public authService(userRepository userRepository, PasswordEncoder passwordEncoder, jwtAuth jwtAuth, TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuth = jwtAuth;
        this.tokenBlacklistService = tokenBlacklistService;
    }


    public BaseResponseDTO<String> logout(String token) {
        try {
            // Extract token from "Bearer " prefix if present
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token);

            return BaseResponseDTO.success("Logged out successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to logout: " + e.getMessage());
        }
    }

    public loginResponseDTO loginService(loginDTO logindto) {
        user user = userRepository.findByEmail(logindto.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if(passwordEncoder.matches(logindto.getPassword(), user.getPassword())) {

            String token = jwtAuth.generateToken(user);
            loginResponseDTO response = new loginResponseDTO();
            response.setToken(token);
//            response.setImageUrl(user.getImagePath()); // if you stored image path
            response.setRole(user.getRole());

            return response;
        }

        throw new RuntimeException("Invalid password");
    }


    public ResponseEntity<Map<String, Object>> signupService(signupDto signupDto) {

        if(userRepository.findByEmail(signupDto.getEmail()).isPresent()) {
            throw new RuntimeException("Cannot register distributor. Try with different email.");
        }

        user user1 = new user();
        user1.setEmail(signupDto.getEmail());
//        user1.setImg(signupDto.getImage());
        user1.setPhone(signupDto.getPhone());
        user1.setUsername(signupDto.getManager_name());

        user1.setRole(Role.DISTRIBUTOR);
        user1.setPassword(passwordEncoder.encode(user1.getEmail().substring(0,5) + user1.getPhone().toString().substring(0,5))); //send pwd through mail to distr
        user1.setStatus(account_status.ACTIVE);

        userRepository.save(user1);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Distributor registered successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


//    logout service
}
