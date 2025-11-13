package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "factories")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "plantHead_id")
    private User plantHead;

    @ManyToOne
    @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @Column(name = "status")
    private AccountStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();


    // Add relationship to userFactory
//    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "factory", cascade = CascadeType.PERSIST)
    private List<UserFactory> userFactories = new ArrayList<>();

    // Helper method to get managers
    public List<User> getManagers() {
        return userFactories.stream()
                .filter(uf -> uf.getUserRole() == Role.MANAGER)
                .map(UserFactory::getUser)
                .collect(Collectors.toList());
    }

    // Helper method to get workers
    public List<User> getWorkers() {
        return userFactories.stream()
                .filter(uf -> uf.getUserRole() == Role.WORKER)
                .map(UserFactory::getUser)
                .collect(Collectors.toList());
    }

    @OneToMany(mappedBy = "factory")
    private List<Bay> bays;
//
    @OneToMany(mappedBy = "factory")
    private List<StorageArea> storageAreas;
}