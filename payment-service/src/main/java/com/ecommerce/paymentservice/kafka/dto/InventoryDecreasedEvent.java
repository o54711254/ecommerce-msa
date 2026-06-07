package com.ecommerce.paymentservice.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryDecreasedEvent(
        Long orderId,
        Long memberId,
        Long amount
) {
}
