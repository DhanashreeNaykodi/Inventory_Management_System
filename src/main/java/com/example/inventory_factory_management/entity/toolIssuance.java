package com.example.inventory_factory_management.entity;

import com.example.inventory_factory_management.constants.toolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_issuance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class toolIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tool_request_id")
    private toolRequest request;

    //in lavanya's code
//    @ManyToOne @JoinColumn(name = "tool_id")
//    private Tool tool;

    @Enumerated(EnumType.STRING)
    private toolIssuanceStatus status = toolIssuanceStatus.ISSUED;
}
