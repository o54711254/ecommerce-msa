package com.ecommerce.orderservice.kafka.dto;

import java.util.List;

public record OrderFailedEvent(
        Long orderId,
        List<OrderItemInfo> itemInfoList
) {
}
