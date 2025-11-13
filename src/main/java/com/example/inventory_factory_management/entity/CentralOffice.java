package com.example.inventory_factory_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "central_office")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CentralOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long centralOfficeId;

    @Column(name = "location")
    private String location;

    @OneToMany(mappedBy = "office", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCentralOffice> userMappings;

    @OneToMany(mappedBy = "centralOffice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Factory> factories;

}

