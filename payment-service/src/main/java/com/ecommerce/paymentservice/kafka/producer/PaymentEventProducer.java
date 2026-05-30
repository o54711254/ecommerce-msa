package com.ecommerce.paymentservice.kafka.producer;

import com.ecommerce.paymentservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        try {
            kafkaTemplate.send("payment.success", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {
        try {
            kafkaTemplate.send("payment.failed", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
