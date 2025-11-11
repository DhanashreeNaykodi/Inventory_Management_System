package com.example.inventory_factory_management.entity;


import com.example.inventory_factory_management.entity.user;
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
public class customer_distributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private user customer;

    @ManyToOne
    @JoinColumn(name = "distributor_id")
    private user distributor;

//    private Instant assignedAt = Instant.now();
}