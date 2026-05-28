package com.ecommerce.paymentservice.domain.dto.req;

public record PaymentWebhookRequest(
        Long orderId,
        String status
) {
}
