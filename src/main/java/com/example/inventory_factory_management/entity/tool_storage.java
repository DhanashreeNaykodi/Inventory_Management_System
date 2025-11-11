package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.storageArea;
import com.example.inventory_factory_management.entity.tool;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tool_storage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class tool_storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private factory factory;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private tool tool;

    @ManyToOne
    @JoinColumn(name = "storage_area_id")
    private storageArea storageArea;

    private Instant assignedAt = Instant.now();
}
