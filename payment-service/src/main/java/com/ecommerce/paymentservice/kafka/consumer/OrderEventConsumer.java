package com.ecommerce.paymentservice.kafka.consumer;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.service.PaymentService;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import com.ecommerce.paymentservice.kafka.dto.InventoryDecreasedEvent;
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

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopic.TopicName.INVENTORY_DECREASED, groupId = "paymentGroup")
    public void handleInventoryDecreased(String rawJson) {
        InventoryDecreasedEvent event;
        try {
            event = objectMapper.readValue(rawJson, InventoryDecreasedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[inventory.decreased] consumed: orderId={}", event.orderId());
        try {
            paymentService.createPayment(KafkaTopic.INVENTORY_DECREASED, event.memberId(),
                    new CreatePaymentRequest(event.orderId(), event.amount()));
        } catch (DataIntegrityViolationException e) {
            log.warn("[inventory.decreased] 중복 이벤트 감지(UNIQUE 위반), 스킵: orderId={}", event.orderId());
        }
    }
}
