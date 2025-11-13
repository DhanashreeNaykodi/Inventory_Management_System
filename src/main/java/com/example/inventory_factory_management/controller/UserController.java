package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.service.CentralOfficeService;
import com.example.inventory_factory_management.service.AuthService;
import com.example.inventory_factory_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    @Autowired
    CentralOfficeService centralOfficeService;

    @GetMapping("/profile")
    public BaseResponseDTO<UserProfileWithFactoryDTO> getUserProfile() {
        return userService.getUserProfile();
    }

    // For OWNER only - get any user's profile with factories
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{userId}/profile")
    public ResponseEntity<BaseResponseDTO<UserProfileWithFactoryDTO>> getUserProfileById(@PathVariable Long userId) {
        BaseResponseDTO<UserProfileWithFactoryDTO> response = userService.getUserProfileById(userId);
        return ResponseEntity.ok(response);
    }


    @PutMapping(value = "/{userId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponseDTO<UserDTO>> updateProfile(
            @PathVariable Long userId,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        // Create UserUpdateDTO
        UserUpdateDTO userDTO = new UserUpdateDTO();
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setPhone(phone);

        BaseResponseDTO<UserDTO> response = userService.updateProfile(userId, userDTO, profileImage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponseDTO<String>> logout(
            @RequestHeader("Authorization") String authHeader) {

        BaseResponseDTO<String> response = authService.logout(authHeader);
        return ResponseEntity.ok(response);
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}
