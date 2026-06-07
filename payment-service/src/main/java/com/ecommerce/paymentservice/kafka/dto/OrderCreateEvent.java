package com.ecommerce.paymentservice.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreateEvent(
        Long memberId,
        Long orderId,
        Long amount
) {
}