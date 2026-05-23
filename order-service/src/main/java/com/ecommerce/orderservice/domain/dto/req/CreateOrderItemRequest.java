package com.ecommerce.orderservice.domain.dto.req;

public record CreateOrderItemRequest(
        Long productId,
        int quantity
) {
}
