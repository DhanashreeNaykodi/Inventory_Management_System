package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_distributor")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerDistributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "distributor_id")
    private User distributor;

//    private Instant assignedAt = Instant.now();
}