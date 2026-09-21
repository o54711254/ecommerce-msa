package com.ecommerce.reviewservice.domain.repository.custom

import com.ecommerce.reviewservice.domain.dto.req.ProductReviewSearchRequest
import com.ecommerce.reviewservice.domain.dto.req.ReviewSort
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewQueryResult
import com.ecommerce.reviewservice.domain.entity.QReview.Companion.review
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ReviewRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ReviewRepositoryCustom {

    override fun getProductReviews(
        productId: Long,
        request: ProductReviewSearchRequest,
        pageable: Pageable,
    ): Page<ProductReviewQueryResult> {
        val content = getContent(productId, request, pageable)
        val total = getTotal(productId, request)
        return PageImpl(content, pageable, total)
    }

    private fun getContent(
        productId: Long,
        request: ProductReviewSearchRequest,
        pageable: Pageable,
    ): List<ProductReviewQueryResult> =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    ProductReviewQueryResult::class.java,
                    review.id,
                    review.memberId,
                    review.content,
                    review.rating,
                    review.createdAt,
                    review.updatedAt,
                )
            )
            .from(review)
            .where(
                review.productId.eq(productId),
                eqRating(request.rating),
            )
            .orderBy(*toOrderSpecifiers(request.sortBy))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

    private fun getTotal(
        productId: Long,
        request: ProductReviewSearchRequest,
    ): Long =
        jpaQueryFactory
            .select(review.count())
            .from(review)
            .where(
                review.productId.eq(productId),
                eqRating(request.rating),
            )
            .fetchOne() ?: 0L

    private fun eqRating(rating: Int?): BooleanExpression? =
        rating?.let { review.rating.eq(it) }

    private fun toOrderSpecifiers(sort: ReviewSort): Array<OrderSpecifier<*>> = when (sort) {
        ReviewSort.LATEST -> arrayOf(review.createdAt.desc())
        ReviewSort.OLDEST -> arrayOf(review.createdAt.asc())
        ReviewSort.HIGHEST_RATING -> arrayOf(review.rating.desc(), review.createdAt.desc())
        ReviewSort.LOWEST_RATING -> arrayOf(review.rating.asc(), review.createdAt.desc())
    }
}
