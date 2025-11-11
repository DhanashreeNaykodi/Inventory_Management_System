package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "factory_production")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class factoryProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private factory factory;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private product product;

    @Column(name = "produced_qty")
    private Long producedQty;
}

