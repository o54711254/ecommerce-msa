package com.ecommerce.productservice.domain.dto.res;

import java.util.Map;

public record ProductNameResponse(
        Map<Long, String> nameMap
) {
}
