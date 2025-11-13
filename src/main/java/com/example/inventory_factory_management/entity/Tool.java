package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.constants.Expensive;
import com.example.inventory_factory_management.constants.ToolType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ToolCategory category;

    @Column(name = "img")
    private String imageUrl;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ToolType type;

    @Column(name = "isExpensive")
    @Enumerated(EnumType.STRING)
    private Expensive isExpensive;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    private AccountStatus status ;


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

