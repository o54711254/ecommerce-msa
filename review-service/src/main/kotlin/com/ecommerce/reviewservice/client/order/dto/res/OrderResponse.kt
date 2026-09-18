package com.ecommerce.reviewservice.client.order.dto.res

import com.ecommerce.reviewservice.client.order.dto.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: Long,
    val totalPrice: Long,
    val orderStatus: OrderStatus,
    val createdAt: LocalDateTime,
    val items: List<OrderItemResponse>,
    )
