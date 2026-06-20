package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import com.ecommerce.notificationservice.kafka.dto.OrderCancelEvent;
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
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopic.TopicName.ORDER_CANCELLED, groupId = "notificationGroup")
    public void handleOrderCancelled(String rawJson) {
        OrderCancelEvent event;
        try {
            event = objectMapper.readValue(rawJson, OrderCancelEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[order.cancelled] consumed: orderId={}", event.orderId());
        try {
            notificationService.createNotification(KafkaTopic.ORDER_CANCELLED, new CreateNotificationRequest(event));
        } catch (DataIntegrityViolationException e) {
            log.warn("[order.cancelled] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }
}
