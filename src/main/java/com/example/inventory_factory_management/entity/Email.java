package com.example.inventory_factory_management.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender")
    private String sender;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Column(columnDefinition = "text")
    private String body;
}
