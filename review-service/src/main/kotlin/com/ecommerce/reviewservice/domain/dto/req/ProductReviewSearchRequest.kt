package com.ecommerce.reviewservice.domain.dto.req

import io.swagger.v3.oas.annotations.media.Schema

data class ProductReviewSearchRequest(

    @Schema(description = "정렬 기준")
    val sortBy: ReviewSort = ReviewSort.LATEST,
    @Schema(description = "특정 별점 리뷰만 조회")
    val rating: Int? = null
)
