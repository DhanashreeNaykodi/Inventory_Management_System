package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_factory")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserFactory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_factory_id")
    private Long id;

    @ManyToOne
//    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status;

    @ManyToOne
    @JoinColumn(name = "bay_id")
    private Bay bay;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private Role userRole;
}