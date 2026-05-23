package com.ecommerce.productservice.client.inventory.dto.req;

public record CreateInventoryRequest(
        Long productId,
        Integer quantity
) {
}
