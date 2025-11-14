package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.dto.BaseResponseDTO;
import com.example.inventory_factory_management.dto.LoginDTO;
import com.example.inventory_factory_management.dto.LoginResponseDTO;
import com.example.inventory_factory_management.dto.SignupDTO;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.repository.UserRepository;
import com.example.inventory_factory_management.security.JwtAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtAuth jwtAuth;
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtAuth jwtAuth, TokenBlacklistService tokenBlacklistService) {
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
            tokenBlacklistService.blacklistToken(token);
            return BaseResponseDTO.success("Logged out successfully");
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to logout: " + e.getMessage());
        }
    }

    public LoginResponseDTO loginService(LoginDTO logindto) {
        User user = userRepository.findByEmail(logindto.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if(passwordEncoder.matches(logindto.getPassword(), user.getPassword())) {

            String token = jwtAuth.generateToken(user);
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setRole(user.getRole());

            return response;
        }

        throw new RuntimeException("Invalid password");
    }


    public ResponseEntity<Map<String, Object>> signupService(SignupDTO signupDto) {

        if(userRepository.findByEmail(signupDto.getEmail()).isPresent()) {
            throw new RuntimeException("Cannot register distributor. Try with different email.");
        }

        User user1 = new User();
        user1.setEmail(signupDto.getEmail());
//        user1.setImg(signupDto.getImage());
        user1.setPhone(signupDto.getPhone());
        user1.setUsername(signupDto.getManager_name());

        user1.setRole(Role.DISTRIBUTOR);
        user1.setPassword(passwordEncoder.encode(user1.getEmail().substring(0,5) + user1.getPhone().toString().substring(0,5))); //send pwd through mail to distr

        user1.setPassword(passwordEncoder.encode(user1.getEmail().substring(0,5) + user1.getPhone().toString().substring(0,5))); //send pwd through mail to distr
        user1.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user1);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Distributor registered successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


//    logout service
}
