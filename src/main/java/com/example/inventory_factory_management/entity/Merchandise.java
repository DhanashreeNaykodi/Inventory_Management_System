package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchandise")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Merchandise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "text")
    private String image;

    @Column(name = "rewardPoints")
    private Integer rewardPoints;
}
