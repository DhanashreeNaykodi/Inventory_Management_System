package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.orderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "distributor_order")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class distributorOrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "distributor_id")
    private Long distributorId;

    @ManyToOne @JoinColumn(name = "order_item_id")
    private orderItem orderItem;

    @Column(name = "totalPrice")
    private BigDecimal totalPrice;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private orderStatus status = orderStatus.PENDING;

    @Column(name = "rejectReason")
    private String rejectReason;

//    @Column(name = "invoice")
//    private Long invoiceId;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private invoice invoice;
}