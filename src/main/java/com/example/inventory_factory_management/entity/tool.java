package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.constants.expensive;
import com.example.inventory_factory_management.constants.toolType;
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
public class tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private toolCategory category;

    @Column(name = "img")
    private String imageUrl;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private toolType type;

    @Column(name = "isExpensive")
    @Enumerated(EnumType.STRING)
    private expensive isExpensive;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    private account_status status ;


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

