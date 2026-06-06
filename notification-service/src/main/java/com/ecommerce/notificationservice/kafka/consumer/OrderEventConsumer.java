package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.dto.OrderCancelEvent;
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

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "order.cancelled", groupId = "notificationGroup")
    public void handleOrderCancelled(String rawJson) {
        try {
            OrderCancelEvent orderCancelEvent = objectMapper.readValue(rawJson, OrderCancelEvent.class);
            log.info("[order.cancelled] consumed: orderId={}", orderCancelEvent.orderId());
            notificationService.createNotification(new CreateNotificationRequest(orderCancelEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
