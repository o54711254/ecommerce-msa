package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.ecommerce.orderservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.orderservice.kafka.dto.OrderCreateEvent;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreated(OrderCreateEvent event) {
        try {
            kafkaTemplate.send(KafkaTopic.TopicName.ORDER_CREATED, String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOrderFailed(OrderFailedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopic.TopicName.ORDER_FAILED, String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOrderCancelled(OrderCancelEvent event) {
        try {
            kafkaTemplate.send(KafkaTopic.TopicName.ORDER_CANCELLED, String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
