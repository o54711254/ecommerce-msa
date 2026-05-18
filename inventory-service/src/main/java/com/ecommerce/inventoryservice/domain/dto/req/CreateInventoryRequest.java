package com.ecommerce.inventoryservice.domain.dto.req;

public record CreateInventoryRequest(
        Long productId,
        Integer quantity
) {
}
