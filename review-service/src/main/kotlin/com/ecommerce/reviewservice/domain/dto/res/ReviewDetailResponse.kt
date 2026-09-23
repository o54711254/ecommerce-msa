package com.ecommerce.reviewservice.domain.dto.res

import java.time.LocalDateTime

data class ReviewDetailResponse(
    val id: Long,
    val productId: Long,
    val memberId: Long,
    val memberName: String?,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
