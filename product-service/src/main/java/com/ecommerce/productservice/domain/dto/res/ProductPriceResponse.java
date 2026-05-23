package com.ecommerce.productservice.domain.dto.res;

import java.util.Map;

public record ProductPriceResponse(
        Map<Long, Long> priceMap
) {
}
