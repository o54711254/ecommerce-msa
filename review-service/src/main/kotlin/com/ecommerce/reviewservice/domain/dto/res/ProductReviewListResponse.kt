package com.ecommerce.reviewservice.domain.dto.res

import java.time.LocalDateTime

data class ProductReviewListResponse(
    val id: Long,
    val memberId: Long,
    // Repository 조회 시엔 null, Service 에서 member-service Feign 호출로 채워 넣는다
    val memberName: String?,
    val content: String,
    val rating: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
