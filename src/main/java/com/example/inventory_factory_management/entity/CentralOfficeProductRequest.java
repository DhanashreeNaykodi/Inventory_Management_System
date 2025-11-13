package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "central_office_product_request")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CentralOfficeProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne @JoinColumn(name = "factory_id")
    private Factory factory;

    @Column(name = "qtyRequested")
    private Integer qtyRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus status;

    @Column(name = "requested_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "requested_by_user_id")
    private User requestedBy; // Chief officer who made the request
}