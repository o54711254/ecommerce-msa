package com.ecommerce.orderservice.client.product.dto;

import java.util.Map;

public record ProductPriceResponse(
        Map<Long, Long> priceMap
) {
}
