package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.orderservice.kafka.dto.PaymentSuccessEvent;
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

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.success", groupId = "orderGroup")
    public void handlePaymentSuccess(String rawJson) {
        try {
            PaymentSuccessEvent event = objectMapper.readValue(rawJson, PaymentSuccessEvent.class);
            log.info("payment.success consumed: orderId={}", event.orderId());
            orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "orderGroup")
    public void handlePaymentFailed(String rawJson) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(rawJson, PaymentFailedEvent.class);
            log.info("payment.failed consumed: orderId={}", event.orderId());
            orderService.updateOrderStatus(event.orderId(), OrderStatus.FAILED);
            orderService.orderFailed(event.orderId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
