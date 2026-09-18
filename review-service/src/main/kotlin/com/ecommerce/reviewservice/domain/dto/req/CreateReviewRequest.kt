package com.ecommerce.reviewservice.domain.dto.req

/**
 * data class
 * - Data를 담는것이 주 목적인 클래스
 * - equals, hashCode, toString 자동 생성
 * - Java의 record 같은 개념
 */
data class CreateReviewRequest(
    val orderId: Long,
    val productId: Long,
    val rating: Int,
    val content: String,
)