package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderFailed(OrderFailedEvent event) {
        kafkaTemplate.send("order.failed", String.valueOf(event.orderId()), event);
    }
}
