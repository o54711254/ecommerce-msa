package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.domain.service.InventoryService;
import com.ecommerce.inventoryservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.inventoryservice.kafka.dto.OrderFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order.failed", groupId = "inventoryGroup")
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("order.failed consumed: orderId={}", event.orderId());
        inventoryService.increaseProductInventory(event.itemInfoList());
    }

    @KafkaListener(topics = "order.cancelled", groupId = "inventoryGroup")
    public void handleOrderCancelled(OrderCancelEvent event) {
        log.info("order.cancelled consumed: orderId={}", event.orderId());
        inventoryService.increaseProductInventory(event.itemInfoList());
    }
}
