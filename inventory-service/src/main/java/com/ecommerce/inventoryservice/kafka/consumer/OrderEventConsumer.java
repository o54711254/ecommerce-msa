package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.domain.service.InventoryService;
import com.ecommerce.inventoryservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.inventoryservice.kafka.dto.OrderFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.failed", groupId = "inventoryGroup")
    public void handleOrderFailed(String rawJson) {
        try {
            OrderFailedEvent event = objectMapper.readValue(rawJson, OrderFailedEvent.class);
            log.info("[order.failed] consumed: orderId={}", event.orderId());
            inventoryService.increaseProductInventory(event.itemInfoList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "inventoryGroup")
    public void handleOrderCancelled(String rawJson) {
        try {
            OrderCancelEvent event = objectMapper.readValue(rawJson, OrderCancelEvent.class);
            log.info("[order.cancelled] consumed: orderId={}", event.orderId());
            inventoryService.increaseProductInventory(event.itemInfoList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
