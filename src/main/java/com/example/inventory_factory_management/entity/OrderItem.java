package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private DistributorOrderRequest order;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "qty")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal pricePerUnit;
}
