package com.ecommerce.orderservice.kafka.dto;

import java.util.List;

public record OrderCancelEvent(
        Long orderId,
        List<OrderItemInfo> itemInfoList
) {
}
