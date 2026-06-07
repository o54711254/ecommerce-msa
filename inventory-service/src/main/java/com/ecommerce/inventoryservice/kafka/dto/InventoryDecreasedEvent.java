package com.ecommerce.inventoryservice.kafka.dto;

public record InventoryDecreasedEvent(
        Long orderId,
        Long memberId,
        Long amount
) {
}
