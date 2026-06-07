package com.ecommerce.orderservice.kafka.producer;

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
            kafkaTemplate.send("order.created", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOrderFailed(OrderFailedEvent event) {
        try {
            kafkaTemplate.send("order.failed", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOrderCancelled(OrderCancelEvent event) {
        try {
            kafkaTemplate.send("order.cancelled", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
