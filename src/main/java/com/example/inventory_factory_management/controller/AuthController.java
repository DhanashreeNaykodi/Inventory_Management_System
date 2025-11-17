package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/login")
    public LoginResponseDTO loginUser(@Valid @RequestBody LoginDTO logindto) {
        return authService.loginService(logindto);
    }

    @PreAuthorize("hasRole('DISTRIBUTOR')")
    @PostMapping("/signup")
    public BaseResponseDTO<UserDTO> signupDistributor(@Valid @RequestBody RegisterDistributorDTO registerDistributorDTO) {
        return authService.registerDistributor(registerDistributorDTO);
    }

//    @PostMapping("/logout")
//    public ResponseEntity<BaseResponseDTO<String>> logout(@RequestHeader("Authorization") String authHeader) {
//        BaseResponseDTO<String> response = authService.logout(authHeader);
//        return ResponseEntity.ok(response);
//    }
}
