package com.ecommerce.inventoryservice.domain.repository;

import com.ecommerce.inventoryservice.domain.entity.ProcessedEvent;
import com.ecommerce.inventoryservice.kafka.config.KafkaTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByKafkaTopicAndOrderId(KafkaTopic kafkaTopic, Long orderId);
}
