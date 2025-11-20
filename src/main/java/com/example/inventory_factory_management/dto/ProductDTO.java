package com.example.inventory_factory_management.dto;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.validations.ValidImage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 3, max = 50, message = "Product name must be between 3 and 50 characters")
    private String name;

    @JsonIgnore // This prevents MultipartFile from being included in JSON response
    @ValidImage
    private MultipartFile imageFile; // For file upload


    private String image; // For storing the URL after upload


    @NotBlank(message = "Description cannot be blank")
        @Size(min = 10, max = 200, message = "Product description must be between 3 and 50 characters")
    private String prodDescription;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Reward points cannot be null")
    @Positive(message = "Reward points must be positive")
    private Integer rewardPts;

    private AccountStatus status;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}