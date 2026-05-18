package com.ecommerce.productservice.infra.feign;

public record CreateInventoryRequest(
        Long productId,
        Integer quantity
) {
}
