package com.ecommerce.notificationservice.kafka.dto;

public record OrderItemInfo(
        Long productId,
        int quantity
) {
}
