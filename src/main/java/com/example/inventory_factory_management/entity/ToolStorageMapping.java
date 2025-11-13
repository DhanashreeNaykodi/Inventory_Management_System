package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_storage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ToolStorageMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @ManyToOne
    @JoinColumn(name = "storage_area_id")
    private StorageArea storageArea;

    private LocalDateTime createdAt ;
}
