package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "central_office_inventory")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CentralOfficeInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Long quantity = 0L;

    // Track total received from all factories
    @Column(name = "total_received")
    private Long totalReceived = 0L;

    // Constructor for creating new inventory
    public CentralOfficeInventory(Product product, Long initialQuantity) {
        this.product = product;
        this.quantity = initialQuantity;
        this.totalReceived = initialQuantity;
    }

    // Method to add quantity
    public void addQuantity(Long quantityToAdd) {
        this.quantity += quantityToAdd;
        this.totalReceived += quantityToAdd;
    }

    // Method to deduct quantity (for distribution/sales)
    public void deductQuantity(Long quantityToDeduct) {
        if (this.quantity < quantityToDeduct) {
            throw new RuntimeException("Insufficient stock in central office inventory");
        }
        this.quantity -= quantityToDeduct;
    }
}