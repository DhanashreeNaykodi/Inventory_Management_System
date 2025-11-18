package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.ToolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_issuance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ToolIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_request_id")
    private ToolRequest request;


    @Enumerated(EnumType.STRING)
    private ToolIssuanceStatus status = ToolIssuanceStatus.ISSUED;
}
