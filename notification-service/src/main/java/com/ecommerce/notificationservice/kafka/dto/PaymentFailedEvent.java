package com.ecommerce.notificationservice.kafka.dto;

public record PaymentFailedEvent(
        Long orderId,
        Long memberId
) {
}
