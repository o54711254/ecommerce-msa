package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.entity.ProcessedEvent;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByKafkaTopicAndOrderId(KafkaTopic kafkaTopic, Long orderId);
}
