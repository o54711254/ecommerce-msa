package com.ecommerce.productservice.dto;

public record CreateProductRequest(
        String name,
        String description,
        Long price
) {
}
