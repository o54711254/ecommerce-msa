package com.ecommerce.paymentservice.domain.entity;

import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_event_payment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"kafka_topic", "order_id"}))
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "kafka_topic", nullable = false, columnDefinition = "varchar(30)")
    private KafkaTopic kafkaTopic;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    public ProcessedEvent(KafkaTopic kafkaTopic, Long orderId) {
        this.kafkaTopic = kafkaTopic;
        this.orderId = orderId;
    }
}
