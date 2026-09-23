package com.ecommerce.reviewservice.kafka.dto

data class ReviewCreatedEvent(
    val reviewId: Long,
    val productId: Long
)
