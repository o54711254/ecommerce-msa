package com.ecommerce.inventoryservice.kafka.dto;

import com.ecommerce.inventoryservice.domain.dto.req.OrderInfoRequest;

import java.util.List;

public record OrderCreateEvent(
        Long memberId,
        Long orderId,
        Long amount,
        List<OrderInfoRequest> itemInfoList
) {
}