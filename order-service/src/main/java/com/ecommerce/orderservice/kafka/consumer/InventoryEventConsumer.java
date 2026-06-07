package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.dto.InventoryFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.failed", groupId = "orderGroup")
    public void handleInventoryFailed(String rawJson) {
        try {
            InventoryFailedEvent event = objectMapper.readValue(rawJson, InventoryFailedEvent.class);
            log.info("[inventory.failed] consumed: orderId={}", event.orderId());
            orderService.updateOrderStatus(event.orderId(), OrderStatus.FAILED);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
