package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.extensionStatus;
import com.example.inventory_factory_management.entity.tool;
import com.example.inventory_factory_management.entity.user;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_extensions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class toolExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private user worker;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private tool tool;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private extensionStatus status = extensionStatus.APPROVED;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private user approvedBy;

    @Column(columnDefinition = "text")
    private String comment;
}
