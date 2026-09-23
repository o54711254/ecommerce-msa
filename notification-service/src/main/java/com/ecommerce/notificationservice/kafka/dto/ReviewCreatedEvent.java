package com.ecommerce.notificationservice.kafka.dto;

public record ReviewCreatedEvent(
        Long reviewId,
        Long productId
) {
}
