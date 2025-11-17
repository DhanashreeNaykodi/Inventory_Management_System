package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "distributor_order")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DistributorOrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "distributor_name")
    private String distributorName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "totalPrice")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "rejectReason")
    private String rejectReason;

    @Column(name = "order_date")
    private LocalDateTime createdAt = LocalDateTime.now();


//    @ManyToOne
//    @JoinColumn(name = "invoice_id")
//    private Invoice invoice;
}