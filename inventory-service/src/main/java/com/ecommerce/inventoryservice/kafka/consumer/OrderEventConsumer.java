package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.domain.dto.req.DecreaseProductInventoryRequest;
import com.ecommerce.inventoryservice.domain.service.InventoryService;
import com.ecommerce.inventoryservice.global.exception.BusinessException;
import com.ecommerce.inventoryservice.kafka.dto.InventoryDecreasedEvent;
import com.ecommerce.inventoryservice.kafka.dto.InventoryFailedEvent;
import com.ecommerce.inventoryservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.inventoryservice.kafka.dto.OrderCreateEvent;
import com.ecommerce.inventoryservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.inventoryservice.kafka.producer.InventoryEventProducer;
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
    private final InventoryEventProducer inventoryEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created", groupId = "inventoryGroup")
    public void handleOrderCreated(String rawJson) {
        OrderCreateEvent event;
        try {
            event = objectMapper.readValue(rawJson, OrderCreateEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[order.created] consumed: orderId={}", event.orderId());

        try {
            inventoryService.decreaseProductInventoryIdempotent(
                    event.orderId(), new DecreaseProductInventoryRequest(event.itemInfoList()));
            inventoryEventProducer.sendInventoryDecreased(
                    new InventoryDecreasedEvent(event.orderId(), event.memberId(), event.amount()));
        } catch (BusinessException e) {
            // 재고 부족 등 비즈니스 오류 → 재시도 없이 주문 실패 처리
            log.warn("[order.created] inventory decrease failed: orderId={}, reason={}", event.orderId(), e.getMessage());
            inventoryEventProducer.sendInventoryFailed(new InventoryFailedEvent(event.orderId()));
        }
    }

    @KafkaListener(topics = "order.failed", groupId = "inventoryGroup")
    public void handleOrderFailed(String rawJson) {
        try {
            OrderFailedEvent event = objectMapper.readValue(rawJson, OrderFailedEvent.class);
            log.info("[order.failed] consumed: orderId={}", event.orderId());
            inventoryService.increaseProductInventoryIdempotent(event.orderId(), event.itemInfoList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "inventoryGroup")
    public void handleOrderCancelled(String rawJson) {
        try {
            OrderCancelEvent event = objectMapper.readValue(rawJson, OrderCancelEvent.class);
            log.info("[order.cancelled] consumed: orderId={}", event.orderId());
            inventoryService.increaseProductInventoryIdempotent(event.orderId(), event.itemInfoList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
