package com.ecommerce.orderservice.client.product.dto;

import java.util.Map;

public record ProductNameResponse(
        Map<Long, String> nameMap
) {
}
