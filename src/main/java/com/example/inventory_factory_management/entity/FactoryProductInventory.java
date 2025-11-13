package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// All factories product production data  (overall products stock)


@Entity
@Table(name = "factories_inventory")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FactoryProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long stockEntryId;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Long qty;

    @ManyToOne
    @JoinColumn(name = "added_by")
    private User addedBy;
}
