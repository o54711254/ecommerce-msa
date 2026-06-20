package com.ecommerce.notificationservice.domain.repository;

import com.ecommerce.notificationservice.domain.entity.ProcessedEvent;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByKafkaTopicAndOrderId(KafkaTopic kafkaTopic, Long orderId);
}
