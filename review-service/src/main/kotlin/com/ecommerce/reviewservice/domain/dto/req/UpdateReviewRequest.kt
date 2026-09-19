package com.ecommerce.reviewservice.domain.dto.req

data class UpdateReviewRequest(
    val rating: Int,
    val content: String,
)
