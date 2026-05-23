package com.ecommerce.inventoryservice.domain.dto.req;

import java.util.List;

public record DecreaseProductInventoryRequest(
        List<OrderInfoRequest> orderInfoRequest
) {
}
