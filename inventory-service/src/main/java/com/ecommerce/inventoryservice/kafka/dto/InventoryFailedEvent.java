package com.ecommerce.inventoryservice.kafka.dto;

public record InventoryFailedEvent(
        Long orderId
) {
}
