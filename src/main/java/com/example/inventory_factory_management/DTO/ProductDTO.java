package com.example.inventory_factory_management.DTO;

import com.example.inventory_factory_management.constants.account_status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    private String name;

    private MultipartFile imageFile; // For file upload
    private String image; // For storing the URL after upload


    @NotBlank(message = "Description cannot be blank")
    private String prodDescription;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Reward points cannot be null")
    @Positive(message = "Reward points must be positive")
    private Integer rewardPts;

    private account_status status;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}