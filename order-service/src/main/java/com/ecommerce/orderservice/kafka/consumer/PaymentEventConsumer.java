package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment.success", groupId = "orderGroup")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("payment.success consumed: orderId={}", event.orderId());
        orderService.markAsPaid(event.orderId());
    }
}
