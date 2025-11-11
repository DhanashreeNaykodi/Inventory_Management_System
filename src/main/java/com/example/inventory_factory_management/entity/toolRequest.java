package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.toolOrProductRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class toolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private tool tool;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private user worker;

    @Column(name = "requestQty")
    private Long requestQty;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private user approvedBy;

    @Column(name = "reject_reason")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private toolOrProductRequestStatus status = toolOrProductRequestStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}