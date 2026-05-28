package com.ecommerce.paymentservice.kafka.dto;

public record PaymentFailedEvent(
        Long orderId,
        Long memberId
) {
}
