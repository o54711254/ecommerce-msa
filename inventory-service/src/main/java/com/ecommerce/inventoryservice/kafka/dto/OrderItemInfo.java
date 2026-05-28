package com.ecommerce.inventoryservice.kafka.dto;

public record OrderItemInfo(
        Long productId,
        int quantity
) {
}
