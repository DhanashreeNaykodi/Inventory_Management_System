package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "users_centralOffice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCentralOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private CentralOffice office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}


