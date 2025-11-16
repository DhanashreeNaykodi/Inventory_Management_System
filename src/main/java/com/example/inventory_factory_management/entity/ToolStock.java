package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_stock")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "total_quantity", nullable = false)
    private Long totalQuantity = 0L;

    @Column(name = "available_quantity", nullable = false)
    private Long availableQuantity = 0L;


    @Column(name = "issued_quantity", nullable = false)
    private Long issuedQuantity = 0L;

    // Either update your entity to match database column names:
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")  // Add this
    private LocalDateTime lastUpdatedAt;
}