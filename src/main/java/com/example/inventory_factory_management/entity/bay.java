package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.entity.factory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bay")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class bay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bayId")
    private Long bay_id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private factory factory;

    @Column(name = "name")
    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

