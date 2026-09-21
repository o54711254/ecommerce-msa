package com.ecommerce.reviewservice.domain.dto.res

import java.time.LocalDateTime

data class MyReviewListResponse(
    val id: Long,
    val productId: Long,
    // Repository 조회 시엔 null, Service 에서 product-service Feign 호출로 채워 넣는다
    val productName: String?,
    val content: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
