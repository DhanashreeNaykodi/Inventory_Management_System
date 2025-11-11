package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class toolReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_issuance_id")
    private toolIssuance toolIssuance;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private user updatedBy;

    @Column(name = "fitQty")
    private Integer fitQty;

    @Column(name = "unfitQty")
    private Integer unfitQty;

    @Column(name = "extendedQty")
    private Integer extendedQty;
}