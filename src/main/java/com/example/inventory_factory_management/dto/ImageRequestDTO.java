package com.example.inventory_factory_management.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageRequestDTO {
    private MultipartFile image;
}