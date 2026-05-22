package com.ecommerce.inventoryservice.domain.dto.req;

import jakarta.validation.constraints.Min;

public record UpdateInventoryRequest(
        @Min(1) int quantity
) {
}
