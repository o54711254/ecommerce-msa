package com.ecommerce.orderservice.feign.product;

import java.util.Map;

public record ProductPriceResponse(
        Map<Long, Long> priceMap
) {
}
