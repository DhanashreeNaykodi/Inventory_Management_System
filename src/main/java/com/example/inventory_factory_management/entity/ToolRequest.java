package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.ToolOrProductRequestStatus;
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
public class ToolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @Column(name = "requestQty")
    private Long requestQty;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "reject_reason")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private ToolOrProductRequestStatus status = ToolOrProductRequestStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;
    private LocalDateTime dueDate;

    // Extension tracking
    private String extensionReason;
    private Integer extensionCount = 0;

    // Relationship to issuance record
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private ToolIssuance issuance;

//    public void setAutoReturnDate(LocalDateTime ) {
//    }

//    private LocalDateTime autoReturnDate;
//    private Boolean autoReturned = false;
}