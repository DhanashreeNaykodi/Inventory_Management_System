package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "central_office_product_request")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class centralOfficeProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne @JoinColumn(name = "central_office_id")
    private centralOffice centralOffice;

    @ManyToOne @JoinColumn(name = "product_id")
    private product product;

    @ManyToOne @JoinColumn(name = "factory_id")
    private factory factory;

    @Column(name = "qtyRequested")
    private Integer qtyRequested;
}