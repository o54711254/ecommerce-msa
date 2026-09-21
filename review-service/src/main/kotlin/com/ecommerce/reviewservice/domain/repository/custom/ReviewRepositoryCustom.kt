package com.ecommerce.reviewservice.domain.repository.custom

import com.ecommerce.reviewservice.domain.dto.req.ProductReviewSearchRequest
import com.ecommerce.reviewservice.domain.dto.res.MyReviewListResponse
import com.ecommerce.reviewservice.domain.dto.res.MyReviewQueryResult
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewQueryResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReviewRepositoryCustom {

    fun getProductReviews(
        productId: Long,
        request: ProductReviewSearchRequest,
        pageable: Pageable,
    ): Page<ProductReviewQueryResult>

    fun getMyReviews(memberId: Long, pageable: Pageable): Page<MyReviewQueryResult>
}
