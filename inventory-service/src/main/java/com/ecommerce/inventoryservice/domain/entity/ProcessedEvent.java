package com.ecommerce.inventoryservice.domain.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_type", "order_id"}))
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, columnDefinition = "varchar(20)")
    private InventoryEventType eventType;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    public ProcessedEvent(InventoryEventType eventType, Long orderId) {
        this.eventType = eventType;
        this.orderId = orderId;
    }
}
