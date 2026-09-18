package com.ecommerce.reviewservice.domain.service

import com.ecommerce.reviewservice.client.order.OrderClient
import com.ecommerce.reviewservice.client.order.dto.OrderStatus
import com.ecommerce.reviewservice.client.order.dto.res.OrderResponse
import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.entity.Review
import com.ecommerce.reviewservice.domain.repository.ReviewRepository
import com.ecommerce.reviewservice.global.exception.custom.OrderNotPaidException
import com.ecommerce.reviewservice.global.exception.custom.ProductNotInOrderException
import com.ecommerce.reviewservice.global.exception.custom.ReviewAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val orderClient: OrderClient
) {
    @Transactional
    fun createReview(memberId: Long, request: CreateReviewRequest): Long {

        if (reviewRepository.existsByOrderIdAndProductId(request.orderId, request.productId)) throw ReviewAlreadyExistsException()

        val orderResponse: OrderResponse = orderClient.getOrder(memberId, request.orderId)
        if (orderResponse.orderStatus != OrderStatus.PAID) throw OrderNotPaidException()

        // it는 람다의 단일 파라미터에 컴파일러가 자동으로 부여하는 이름, 파라미터가 1개일때만 사용 가능
        val hasProduct = orderResponse.items.any { it.productId == request.productId }
        if (!hasProduct) throw ProductNotInOrderException()

        val review = Review(
            orderId = request.orderId,
            productId = request.productId,
            memberId = memberId,
            rating = request.rating,
            content = request.content,
        )

        return reviewRepository.save(review).id!!   // !!는 null이 아님을 컴파일러에게 알려줌
    }
}