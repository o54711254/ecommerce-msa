package com.ecommerce.productservice.infra.feign.inventory;

public record CreateInventoryRequest(
        Long productId,
        Integer quantity
) {
}
