package com.ecommerce.reviewservice.domain.service

import com.ecommerce.reviewservice.client.member.MemberClient
import com.ecommerce.reviewservice.client.member.dto.MemberInfoResponse
import com.ecommerce.reviewservice.client.order.OrderClient
import com.ecommerce.reviewservice.client.order.dto.OrderStatus
import com.ecommerce.reviewservice.client.order.dto.res.OrderResponse
import com.ecommerce.reviewservice.client.product.ProductClient
import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.dto.req.ProductReviewSearchRequest
import com.ecommerce.reviewservice.domain.dto.req.UpdateReviewRequest
import com.ecommerce.reviewservice.domain.dto.res.MyReviewListResponse
import com.ecommerce.reviewservice.domain.dto.res.MyReviewQueryResult
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewListResponse
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewQueryResult
import com.ecommerce.reviewservice.domain.dto.res.ReviewDetailResponse
import com.ecommerce.reviewservice.domain.entity.Review
import com.ecommerce.reviewservice.domain.repository.ReviewRepository
import com.ecommerce.reviewservice.global.exception.custom.OrderNotPaidException
import com.ecommerce.reviewservice.global.exception.custom.ProductNotInOrderException
import com.ecommerce.reviewservice.global.exception.custom.ReviewAccessDeniedException
import com.ecommerce.reviewservice.global.exception.custom.ReviewAlreadyExistsException
import com.ecommerce.reviewservice.global.exception.custom.ReviewNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val orderClient: OrderClient,
    private val memberClient: MemberClient,
    private val productClient: ProductClient
) {
    @Transactional
    fun createReview(memberId: Long, request: CreateReviewRequest): Long {

        if (reviewRepository.existsByOrderIdAndProductId(
                request.orderId,
                request.productId
            )
        ) throw ReviewAlreadyExistsException()

        val orderResponse: OrderResponse = orderClient.getOrder(memberId, request.orderId)
        if (orderResponse.orderStatus != OrderStatus.PAID) throw OrderNotPaidException()

        // it는 람다의 단일 파라미터에 컴파일러가 자동으로 부여하는 이름, 파라미터가 1개일때만 사용 가능
        val hasProduct = orderResponse.itemList.any { it.productId == request.productId }
        if (!hasProduct) throw ProductNotInOrderException()

        val review = Review.create(
            productId = request.productId,
            orderId = request.orderId,
            memberId = memberId,
            rating = request.rating,
            content = request.content
        )


        return reviewRepository.save(review).id!!   // !!는 null이 아님을 컴파일러에게 알려줌
    }

    @Transactional
    fun deleteReview(memberId: Long, reviewId: Long) {

        // ?: 왼쪽이 null이면 오른쪽 반환, 아니면 왼쪽 반환
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        if (review.memberId != memberId) throw ReviewAccessDeniedException()
        reviewRepository.delete(review)
    }

    @Transactional
    fun updateReview(memberId: Long, reviewId: Long, request: UpdateReviewRequest) {
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        if (review.memberId != memberId) throw ReviewAccessDeniedException()
        review.update(request.rating, request.content)
    }

    @Transactional(readOnly = true)
    fun getProductReview(
        productId: Long,
        request: ProductReviewSearchRequest,
        pageable: Pageable,
    ): Page<ProductReviewListResponse> {
        val queryResult: Page<ProductReviewQueryResult> =
            reviewRepository.getProductReviews(productId, request, pageable)

        val memberIds: List<Long> = queryResult.content.map { it.memberId }.distinct()

        val memberInfos: List<MemberInfoResponse> = if (memberIds.isEmpty()) {
            emptyList()
        } else {
            memberClient.getMemberInfos(memberIds)
        }

        val memberNamesById: Map<Long, String> = memberInfos.associate { it.memberId to it.name }

        return queryResult.map { review ->
            ProductReviewListResponse(
                id = review.id,
                memberId = review.memberId,
                memberName = memberNamesById[review.memberId],
                content = review.content,
                rating = review.rating,
                createdAt = review.createdAt,
                updatedAt = review.updatedAt,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getMyReview(memberId: Long, pageable: Pageable): Page<MyReviewListResponse> {
        val queryResult: Page<MyReviewQueryResult> = reviewRepository.getMyReviews(memberId, pageable)
        val productIds: List<Long> = queryResult.content.map { it.productId }.distinct()
        val nameMap: Map<Long, String> = productClient.getProductNames(productIds).nameMap
        //원래 버전
//        val result: List<MyReviewListResponse> = queryResult.content.map { review ->
//            MyReviewListResponse(
//                id = review.id,
//                productId = review.productId,
//                productName = nameMap[review.productId],  // map[key] = map.get(key)
//                rating = review.rating,
//                content = review.content,
//                createdAt = review.createdAt,
//                updatedAt = review.updatedAt,
//            )
//        }
//        return PageImpl(result, pageable, queryResult.totalElements)

        // 편의 메서드
        return queryResult.map {
            MyReviewListResponse(
                id = it.id,
                productId = it.productId,
                productName = nameMap[it.productId],
                rating = it.rating,
                content = it.content,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }

    }

    @Transactional(readOnly = true)
    fun getReviewDetail(reviewId: Long): ReviewDetailResponse {
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw ReviewNotFoundException()
        val memberMap: Map<Long, String> =
            memberClient.getMemberInfos(listOf(review.memberId)).associate { it.memberId to it.name }
        return ReviewDetailResponse(
            id = reviewId,
            productId = review.productId,
            memberId = review.memberId,
            memberName = memberMap[review.memberId],
            rating = review.rating,
            content = review.content,
            createdAt = review.createdAt!!,
            updatedAt = review.updatedAt!!
        )
    }
}
