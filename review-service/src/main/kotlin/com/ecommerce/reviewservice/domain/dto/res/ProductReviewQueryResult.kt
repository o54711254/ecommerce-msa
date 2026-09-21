package com.ecommerce.reviewservice.domain.dto.res

import java.time.LocalDateTime

data class ProductReviewQueryResult(
    val id: Long,
    val memberId: Long,
    val content: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
