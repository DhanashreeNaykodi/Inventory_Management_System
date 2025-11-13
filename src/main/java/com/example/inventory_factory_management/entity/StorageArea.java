package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "storage_area")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

//    in lavanya's code
    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @OneToMany(mappedBy = "storageArea", cascade = CascadeType.ALL)
    private List<ToolStorageMapping> toolStorageMappings;

    @JoinColumn(name = "rowNum")
    private Integer rowNum;

    @JoinColumn(name = "colNum")
    private Integer colNum;

    @JoinColumn(name = "stack")
    private Integer stack;

    @JoinColumn(name = "bucket")
    private String bucket;

}