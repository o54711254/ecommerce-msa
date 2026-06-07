package com.ecommerce.orderservice.kafka.dto;

import java.util.List;

public record OrderCreateEvent(
        Long memberId,
        Long orderId,
        Long amount,
        List<OrderItemInfo> itemInfoList
) {
}
