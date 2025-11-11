package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.account_status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private productCategory category;

    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "text")
    private String image;

    @Column(columnDefinition = "text")
    private String prodDescription;

    @Column(name = "price")
    private java.math.BigDecimal price;

    @Column(name = "rewardPts")
    private Integer rewardPts;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private account_status status;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

