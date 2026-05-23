package com.ecommerce.orderservice.client.inventory.dto;

public record OrderInfoRequest(
        Long productId,
        int quantity
) {
}