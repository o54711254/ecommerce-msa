package com.ecommerce.productservice.domain.dto.res;

import com.ecommerce.productservice.domain.entity.ProductStatus;

public record ProductListResponse(
        String productName,
        Long price,
        ProductStatus status
) {
}
