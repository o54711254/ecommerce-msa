package com.ecommerce.orderservice.kafka.dto;

public record OrderItemInfo(
        Long productId,
        int quantity
) {
}
