package com.ecommerce.paymentservice.kafka.producer;

import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        // topic, key, data
        kafkaTemplate.send("payment.success", String.valueOf(event.orderId()), event);
    }
}
