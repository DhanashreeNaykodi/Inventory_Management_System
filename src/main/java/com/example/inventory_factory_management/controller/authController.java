package com.example.inventory_factory_management.controller;


import com.example.inventory_factory_management.DTO.loginDTO;
import com.example.inventory_factory_management.DTO.loginResponseDTO;
import com.example.inventory_factory_management.DTO.signupDto;
import com.example.inventory_factory_management.service.authService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class authController {

    @Autowired
    authService authService;


    @PostMapping("/login")
    public loginResponseDTO loginUser(@Valid @RequestBody loginDTO logindto) {
        return authService.loginService(logindto);
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signupDistributor(@Valid @RequestBody signupDto signupDto) {
        return authService.signupService(signupDto);
    }

//    @PostMapping("/logout")
//    public ResponseEntity<BaseResponseDTO<String>> logout(@RequestHeader("Authorization") String authHeader) {
//        BaseResponseDTO<String> response = authService.logout(authHeader);
//        return ResponseEntity.ok(response);
//    }
}
