package com.ecommerce.orderservice.kafka.dto;

public record PaymentSuccessEvent(
        Long orderId,
        Long memberId,
        Long amount
) {
}
