package com.ecommerce.orderservice.domain.dto.res;

import com.ecommerce.orderservice.domain.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderListResponse(
        Long orderId,
        OrderStatus orderStatus,
        Long totalPrice,
        LocalDateTime createdAt
) {
}
