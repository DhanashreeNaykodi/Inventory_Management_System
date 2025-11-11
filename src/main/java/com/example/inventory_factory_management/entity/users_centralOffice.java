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
public class users_centralOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private centralOffice office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private user user;
}


