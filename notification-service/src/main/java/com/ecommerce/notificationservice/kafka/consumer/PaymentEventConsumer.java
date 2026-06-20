package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import com.ecommerce.notificationservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentSuccessEvent;
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
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.TopicName.PAYMENT_SUCCESS, groupId = "notificationGroup")
    public void handlePaymentSuccess(String rawJson) {
        PaymentSuccessEvent event;
        try {
            event = objectMapper.readValue(rawJson, PaymentSuccessEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[payment.success] consumed: orderId={}", event.orderId());
        try {
            notificationService.createNotification(KafkaTopic.PAYMENT_SUCCESS, new CreateNotificationRequest(event));
        } catch (DataIntegrityViolationException e) {
            log.warn("[payment.success] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }

    @KafkaListener(topics = KafkaTopic.TopicName.PAYMENT_FAILED, groupId = "notificationGroup")
    public void handlePaymentFailed(String rawJson) {
        PaymentFailedEvent event;
        try {
            event = objectMapper.readValue(rawJson, PaymentFailedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[payment.failed] consumed: orderId={}", event.orderId());
        try {
            notificationService.createNotification(KafkaTopic.PAYMENT_FAILED, new CreateNotificationRequest(event));
        } catch (DataIntegrityViolationException e) {
            log.warn("[payment.failed] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }
}
