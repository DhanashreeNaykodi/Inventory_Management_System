//package com.example.inventory_factory_management.entity;
//
//import com.example.inventory_factory_management.constants.ExtensionStatus;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "tool_extensions")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
//@Setter
//public class ToolExtension {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "worker_id")
//    private User worker;
//
//    @ManyToOne
//    @JoinColumn(name = "tool_id")
//    private Tool tool;
//
//    @Column(name = "status")
//    @Enumerated(EnumType.STRING)
//    private ExtensionStatus status = ExtensionStatus.APPROVED;
//
//    @ManyToOne
//    @JoinColumn(name = "approved_by")
//    private User approvedBy;
//
//    @Column(columnDefinition = "text")
//    private String comment;
//}
