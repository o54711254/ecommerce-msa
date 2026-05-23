package com.ecommerce.productservice.client.inventory.dto.res;

public record InventoryResponse(
        Long productId,
        Integer quantity
) {
}
