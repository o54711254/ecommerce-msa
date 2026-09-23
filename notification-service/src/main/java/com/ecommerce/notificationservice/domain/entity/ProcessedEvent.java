package com.ecommerce.notificationservice.domain.entity;

import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_event_notification",
        uniqueConstraints = @UniqueConstraint(columnNames = {"kafka_topic", "target_id"}))
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "kafka_topic", nullable = false, columnDefinition = "varchar(30)")
    private KafkaTopic kafkaTopic;

    // kafkaTopic이 discriminator라 topic 안에서만 유일하면 됨
    // (PAYMENT_SUCCESS → orderId, REVIEW_CREATED → reviewId 등 topic에 따라 의미 다름)
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    public ProcessedEvent(KafkaTopic kafkaTopic, Long targetId) {
        this.kafkaTopic = kafkaTopic;
        this.targetId = targetId;
    }
}
