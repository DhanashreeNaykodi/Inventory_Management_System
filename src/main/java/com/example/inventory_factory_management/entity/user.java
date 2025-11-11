package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.bay;
import com.example.inventory_factory_management.entity.factory;
import com.example.inventory_factory_management.entity.userFactory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(columnDefinition = "TEXT") // Use TEXT for unlimited length
    private String img;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "phone")
    private Long phone;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private account_status status = account_status.ACTIVE;


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Add relationship to userFactory
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<userFactory> userFactories = new ArrayList<>();

    // Helper method to add factory with role
    public void addFactory(factory factory, Role role, bay bay) {
        userFactory userFactory = new userFactory();
        userFactory.setUser(this);
        userFactory.setFactory(factory);
        userFactory.setUserRole(role);
        userFactory.setBay(bay);
        this.userFactories.add(userFactory);
    }

}
