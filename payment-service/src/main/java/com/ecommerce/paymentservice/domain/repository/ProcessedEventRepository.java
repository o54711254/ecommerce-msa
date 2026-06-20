package com.ecommerce.paymentservice.domain.repository;

import com.ecommerce.paymentservice.domain.entity.ProcessedEvent;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByKafkaTopicAndOrderId(KafkaTopic kafkaTopic, Long orderId);
}
