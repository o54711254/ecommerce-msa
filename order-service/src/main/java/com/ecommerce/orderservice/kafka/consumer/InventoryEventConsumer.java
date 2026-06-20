package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.ecommerce.orderservice.kafka.dto.InventoryFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.TopicName.INVENTORY_FAILED, groupId = "orderGroup")
    public void handleInventoryFailed(String rawJson) {
        InventoryFailedEvent event;
        try {
            event = objectMapper.readValue(rawJson, InventoryFailedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[inventory.failed] consumed: orderId={}", event.orderId());
        try {
            orderService.updateOrderStatus(KafkaTopic.INVENTORY_FAILED, event.orderId(), OrderStatus.FAILED);
        } catch (DataIntegrityViolationException e) {
            log.warn("[inventory.failed] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }
}
