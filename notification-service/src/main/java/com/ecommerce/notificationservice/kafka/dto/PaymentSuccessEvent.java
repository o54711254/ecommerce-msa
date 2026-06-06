package com.ecommerce.notificationservice.kafka.dto;

public record PaymentSuccessEvent(
        Long orderId,
        Long memberId,
        Long paymentId,
        Long amount
) {
}
