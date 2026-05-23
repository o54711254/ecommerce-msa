package com.ecommerce.orderservice.client.inventory.dto;

import java.util.List;

public record DecreaseProductInventoryRequest(
        List<OrderInfoRequest> orderInfoRequest
) {
}
