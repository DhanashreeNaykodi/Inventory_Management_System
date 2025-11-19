package com.example.inventory_factory_management.dto;


import com.example.inventory_factory_management.validations.ValidImage;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageRequestDTO {

    @ValidImage
    private MultipartFile image;
}