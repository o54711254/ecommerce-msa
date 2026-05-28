package com.ecommerce.orderservice.kafka.dto;

public record PaymentFailedEvent(
        Long orderId,
        Long memberId
) {
}
