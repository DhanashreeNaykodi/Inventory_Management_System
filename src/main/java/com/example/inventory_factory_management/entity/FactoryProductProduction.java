package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.*;

// Per factory product production data

@Entity
@Table(name = "factory_production")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FactoryProductProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "produced_qty")
    private Long producedQty;
}

