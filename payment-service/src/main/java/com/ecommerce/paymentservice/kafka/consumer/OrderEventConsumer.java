package com.ecommerce.paymentservice.kafka.consumer;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.service.PaymentService;
import com.ecommerce.paymentservice.kafka.dto.InventoryDecreasedEvent;
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

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.decreased", groupId = "paymentGroup")
    public void handleInventoryDecreased(String rawJson) {
        try {
            InventoryDecreasedEvent event = objectMapper.readValue(rawJson, InventoryDecreasedEvent.class);
            log.info("[inventory.decreased] consumed: orderId={}", event.orderId());
            paymentService.createPayment(event.memberId(), new CreatePaymentRequest(event.orderId(), event.amount()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
