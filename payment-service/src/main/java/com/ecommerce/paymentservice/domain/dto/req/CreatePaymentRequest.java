package com.ecommerce.paymentservice.domain.dto.req;

public record CreatePaymentRequest(
        Long orderId,
        Long amount
) {
}
