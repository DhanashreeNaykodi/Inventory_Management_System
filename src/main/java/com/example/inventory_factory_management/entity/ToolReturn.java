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
public class ToolReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_issuance_id")
    private ToolIssuance toolIssuance;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "fitQty")
    private Integer fitQty;

    @Column(name = "unfitQty")
    private Integer unfitQty;

    @Column(name = "extendedQty")
    private Integer extendedQty;
}