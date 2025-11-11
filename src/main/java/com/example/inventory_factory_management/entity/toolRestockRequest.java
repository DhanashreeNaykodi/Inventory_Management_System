package com.example.inventory_factory_management.entity;


import com.example.inventory_factory_management.constants.toolOrProductRequestStatus;
import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.tool;
import com.example.inventory_factory_management.entity.user;
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
public class toolRestockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restocked_by")
    private user restockedBy;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private tool tool;

    @Column(name = "qty")
    private Long toolQty;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private factory factory;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private toolOrProductRequestStatus status = toolOrProductRequestStatus.PENDING;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
