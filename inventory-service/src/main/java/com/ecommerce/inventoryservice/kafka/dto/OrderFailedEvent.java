package com.ecommerce.inventoryservice.kafka.dto;

import java.util.List;

public record OrderFailedEvent(
        Long orderId,
        List<OrderItemInfo> itemInfoList
) {
}