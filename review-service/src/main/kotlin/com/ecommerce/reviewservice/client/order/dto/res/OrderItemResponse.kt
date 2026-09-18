package com.ecommerce.reviewservice.client.order.dto.res

data class OrderItemResponse(
    val orderItemId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val itemPrice: Long,
    val totalPrice: Long,
)
