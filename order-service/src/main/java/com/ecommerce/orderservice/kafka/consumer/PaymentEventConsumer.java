package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.orderservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.orderservice.kafka.dto.PaymentSuccessEvent;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
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
        orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);
    }

    @KafkaListener(topics = "payment.failed", groupId = "orderGroup")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("payment.failed consumed: orderId={}", event.orderId());
        orderService.updateOrderStatus(event.orderId(), OrderStatus.FAILED);
        orderService.orderFailed(event.orderId());
    }
}
