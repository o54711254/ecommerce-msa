package com.ecommerce.orderservice.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryFailedEvent(
        Long orderId
) {
}
