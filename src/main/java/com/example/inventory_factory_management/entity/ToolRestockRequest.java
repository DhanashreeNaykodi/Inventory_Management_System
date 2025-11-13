package com.example.inventory_factory_management.entity;


import com.example.inventory_factory_management.constants.ToolOrProductRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_restock_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ToolRestockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restocked_by")
    private User restockedBy;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @Column(name = "qty")
    private Long toolQty;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ToolOrProductRequestStatus status = ToolOrProductRequestStatus.PENDING;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
