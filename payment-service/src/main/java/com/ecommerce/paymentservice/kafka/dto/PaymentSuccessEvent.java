package com.ecommerce.paymentservice.kafka.dto;

public record PaymentSuccessEvent(
        Long orderId,
        Long memberId,
        Long amount
) {
}
