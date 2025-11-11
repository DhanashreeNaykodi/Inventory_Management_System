package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "factories_inventory")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class factoryInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long stockEntryId;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private factory factory;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private product product;

    @Column(name = "quantity")
    private Long qty;

    @ManyToOne
    @JoinColumn(name = "added_by")
    private user addedBy;
}
