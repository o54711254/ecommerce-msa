package com.ecommerce.orderservice.client.payment.dto.req;

public record CreatePaymentRequest(
        Long orderId,
        Long amount
) {
}

