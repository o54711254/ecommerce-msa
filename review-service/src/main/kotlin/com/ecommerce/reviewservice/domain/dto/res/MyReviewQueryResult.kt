package com.ecommerce.reviewservice.domain.dto.res

import java.time.LocalDateTime

data class MyReviewQueryResult(
    val id: Long,
    val productId: Long,
    val content: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
