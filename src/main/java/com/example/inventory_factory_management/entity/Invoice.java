package com.example.inventory_factory_management.entity;


import com.example.inventory_factory_management.constants.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "invoice")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "order_id")
    private DistributorOrderRequest order;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

//    private Long distributorId;

    @Column(name = "user_role")
    private Role role;

    @Column(columnDefinition = "text")
    private String csvFileUrl;

    @Column(columnDefinition = "text")
    private String pdfUrl;

    private LocalDate date;

    @Column(name = "totalAmount")
    private java.math.BigDecimal totalAmount;
}