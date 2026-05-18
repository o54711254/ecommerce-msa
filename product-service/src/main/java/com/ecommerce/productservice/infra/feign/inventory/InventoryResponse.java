package com.ecommerce.productservice.infra.feign.inventory;

public record InventoryResponse(
        Long productId,
        Integer quantity
) {
}
