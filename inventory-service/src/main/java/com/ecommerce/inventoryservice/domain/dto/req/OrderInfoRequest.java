package com.ecommerce.inventoryservice.domain.dto.req;

public record OrderInfoRequest(
        Long productId,
        int quantity
) {
}
