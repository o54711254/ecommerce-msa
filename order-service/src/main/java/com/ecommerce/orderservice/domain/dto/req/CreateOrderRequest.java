package com.ecommerce.orderservice.domain.dto.req;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderItemRequest> items
) {
}
