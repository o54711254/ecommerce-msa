package com.ecommerce.productservice.domain.dto.req;

public record CreateProductRequest(
        String name,
        String description,
        Long price,
        Integer quantity
) {
}
