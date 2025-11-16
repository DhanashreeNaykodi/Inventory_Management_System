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

    @OneToMany(mappedBy = "storageArea", cascade = CascadeType.ALL)
    private List<ToolStorageMapping> toolStorageMappings;

    @Column(name = "rowNum")
    private Integer rowNum;

    @Column(name = "colNum")
    private Integer colNum;

    @Column(name = "stack")
    private Integer stack;

    @Column(name = "bucket")
    private String bucket;

    // Add this field for easy location code access
    @Column(name = "location_code", unique = true)
    private String locationCode;

    // Add this method to generate location code
    @PrePersist
    @PreUpdate
    public void generateLocationCode() {
        if (this.rowNum != null && this.colNum != null && this.stack != null && this.bucket != null) {
            this.locationCode = "R" + this.rowNum + "C" + this.colNum + "S" + this.stack + "B" + this.bucket;
        }
    }
}