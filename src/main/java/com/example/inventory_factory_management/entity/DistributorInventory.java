package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distributor_inventory")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DistributorInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "distributor_id")
    private Long distributorId;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "qty")
    private Integer stockQty;
}
