package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.ecommerce.orderservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.orderservice.kafka.dto.PaymentSuccessEvent;
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

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.TopicName.PAYMENT_SUCCESS, groupId = "orderGroup")
    public void handlePaymentSuccess(String rawJson) {
        PaymentSuccessEvent event;
        try {
            event = objectMapper.readValue(rawJson, PaymentSuccessEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[payment.success] consumed: orderId={}", event.orderId());
        try {
            orderService.updateOrderStatus(KafkaTopic.PAYMENT_SUCCESS, event.orderId(), OrderStatus.PAID);
        } catch (DataIntegrityViolationException e) {
            log.warn("[payment.success] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }

    @KafkaListener(topics = KafkaTopic.TopicName.PAYMENT_FAILED, groupId = "orderGroup")
    public void handlePaymentFailed(String rawJson) {
        PaymentFailedEvent event;
        try {
            event = objectMapper.readValue(rawJson, PaymentFailedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[payment.failed] consumed: orderId={}", event.orderId());
        try {
            orderService.handlePaymentFailed(KafkaTopic.PAYMENT_FAILED, event.orderId());
        } catch (DataIntegrityViolationException e) {
            log.warn("[payment.failed] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }
}
