package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentSuccessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.success", groupId = "notificationGroup")
    public void handlePaymentSuccess(String rawJson) {
        try {
            PaymentSuccessEvent paymentSuccessEvent = objectMapper.readValue(rawJson, PaymentSuccessEvent.class);
            log.info("[payment.success] consumed: orderId={}", paymentSuccessEvent.orderId());
            notificationService.createNotification(new CreateNotificationRequest(paymentSuccessEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notificationGroup")
    public void handlePaymentFailed(String rawJson) {
        try {
            PaymentFailedEvent paymentFailedEvent = objectMapper.readValue(rawJson, PaymentFailedEvent.class);
            log.info("[payment.failed] consumed: orderId={}", paymentFailedEvent.orderId());
            notificationService.createNotification(new CreateNotificationRequest(paymentFailedEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
