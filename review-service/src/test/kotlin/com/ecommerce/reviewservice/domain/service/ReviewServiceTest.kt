package com.ecommerce.reviewservice.domain.service

import com.ecommerce.reviewservice.client.member.MemberClient
import com.ecommerce.reviewservice.client.member.dto.MemberInfoResponse
import com.ecommerce.reviewservice.client.order.OrderClient
import com.ecommerce.reviewservice.client.order.dto.OrderStatus
import com.ecommerce.reviewservice.client.order.dto.res.OrderItemResponse
import com.ecommerce.reviewservice.client.order.dto.res.OrderResponse
import com.ecommerce.reviewservice.client.product.ProductClient
import com.ecommerce.reviewservice.client.product.dto.ProductNameResponse
import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.dto.req.ProductReviewSearchRequest
import com.ecommerce.reviewservice.domain.dto.req.ReviewSort
import com.ecommerce.reviewservice.domain.dto.req.UpdateReviewRequest
import com.ecommerce.reviewservice.domain.dto.res.MyReviewQueryResult
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewQueryResult
import com.ecommerce.reviewservice.domain.entity.Review
import com.ecommerce.reviewservice.domain.repository.ReviewRepository
import com.ecommerce.reviewservice.global.exception.custom.OrderNotPaidException
import com.ecommerce.reviewservice.global.exception.custom.ProductNotInOrderException
import com.ecommerce.reviewservice.global.exception.custom.ReviewAccessDeniedException
import com.ecommerce.reviewservice.global.exception.custom.ReviewAlreadyExistsException
import com.ecommerce.reviewservice.global.exception.custom.ReviewNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {

    @Mock private lateinit var reviewRepository: ReviewRepository
    @Mock private lateinit var orderClient: OrderClient
    @Mock private lateinit var memberClient: MemberClient
    @Mock private lateinit var productClient: ProductClient

    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        reviewService = ReviewService(reviewRepository, orderClient, memberClient, productClient)
    }

    @Nested
    @DisplayName("createReview - 리뷰 생성")
    inner class CreateReviewTest {

        private val memberId = 1L
        private val request = CreateReviewRequest(orderId = 10L, productId = 100L, rating = 5, content = "좋아요")

        @Test
        fun 성공() {
            val orderResponse = OrderResponse(
                orderId = 10L,
                totalPrice = 5000L,
                orderStatus = OrderStatus.PAID,
                createdAt = LocalDateTime.now(),
                itemList = listOf(OrderItemResponse(1L, 100L, "상품", 1, 5000L, 5000L)),
            )
            val savedReview = Review(id = 999L, orderId = 10L, productId = 100L, memberId = memberId, rating = 5, content = "좋아요")

            given(reviewRepository.existsByOrderIdAndProductId(10L, 100L)).willReturn(false)
            given(orderClient.getOrder(memberId, 10L)).willReturn(orderResponse)
            given(reviewRepository.save(any(Review::class.java))).willReturn(savedReview)

            val result = reviewService.createReview(memberId, request)

            assertThat(result).isEqualTo(999L)
        }

        @Test
        fun 실패_중복_리뷰() {
            given(reviewRepository.existsByOrderIdAndProductId(10L, 100L)).willReturn(true)

            assertThatThrownBy { reviewService.createReview(memberId, request) }
                .isInstanceOf(ReviewAlreadyExistsException::class.java)

            verify(orderClient, never()).getOrder(anyLong(), anyLong())
        }

        @Test
        fun 실패_결제_안됨() {
            val orderResponse = OrderResponse(
                orderId = 10L,
                totalPrice = 5000L,
                orderStatus = OrderStatus.PENDING,
                createdAt = LocalDateTime.now(),
                itemList = emptyList(),
            )
            given(reviewRepository.existsByOrderIdAndProductId(10L, 100L)).willReturn(false)
            given(orderClient.getOrder(memberId, 10L)).willReturn(orderResponse)

            assertThatThrownBy { reviewService.createReview(memberId, request) }
                .isInstanceOf(OrderNotPaidException::class.java)
        }

        @Test
        fun 실패_주문에_상품_없음() {
            val orderResponse = OrderResponse(
                orderId = 10L,
                totalPrice = 5000L,
                orderStatus = OrderStatus.PAID,
                createdAt = LocalDateTime.now(),
                itemList = listOf(OrderItemResponse(1L, 999L, "다른상품", 1, 5000L, 5000L)),
            )
            given(reviewRepository.existsByOrderIdAndProductId(10L, 100L)).willReturn(false)
            given(orderClient.getOrder(memberId, 10L)).willReturn(orderResponse)

            assertThatThrownBy { reviewService.createReview(memberId, request) }
                .isInstanceOf(ProductNotInOrderException::class.java)
        }
    }

    @Nested
    @DisplayName("deleteReview - 리뷰 삭제")
    inner class DeleteReviewTest {

        @Test
        fun 성공() {
            val review = Review(id = 1L, orderId = 10L, productId = 1L, memberId = 1L, rating = 5, content = "x")
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))

            reviewService.deleteReview(memberId = 1L, reviewId = 1L)

            verify(reviewRepository).delete(review)
        }

        @Test
        fun 실패_리뷰_없음() {
            given(reviewRepository.findById(1L)).willReturn(Optional.empty())

            assertThatThrownBy { reviewService.deleteReview(1L, 1L) }
                .isInstanceOf(ReviewNotFoundException::class.java)

            verify(reviewRepository, never()).delete(any(Review::class.java))
        }

        @Test
        fun 실패_권한_없음() {
            val review = Review(id = 1L, orderId = 10L, productId = 1L, memberId = 999L, rating = 5, content = "x")
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))

            assertThatThrownBy { reviewService.deleteReview(memberId = 1L, reviewId = 1L) }
                .isInstanceOf(ReviewAccessDeniedException::class.java)

            verify(reviewRepository, never()).delete(any(Review::class.java))
        }
    }

    @Nested
    @DisplayName("updateReview - 리뷰 수정")
    inner class UpdateReviewTest {

        @Test
        fun 성공() {
            val review = Review(id = 1L, orderId = 10L, productId = 1L, memberId = 1L, rating = 3, content = "old")
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))

            reviewService.updateReview(memberId = 1L, reviewId = 1L, request = UpdateReviewRequest(rating = 5, content = "new"))

            assertThat(review.rating).isEqualTo(5)
            assertThat(review.content).isEqualTo("new")
        }

        @Test
        fun 실패_리뷰_없음() {
            given(reviewRepository.findById(1L)).willReturn(Optional.empty())

            assertThatThrownBy {
                reviewService.updateReview(1L, 1L, UpdateReviewRequest(5, "x"))
            }.isInstanceOf(ReviewNotFoundException::class.java)
        }

        @Test
        fun 실패_권한_없음() {
            val review = Review(id = 1L, orderId = 10L, productId = 1L, memberId = 999L, rating = 5, content = "x")
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))

            assertThatThrownBy {
                reviewService.updateReview(memberId = 1L, reviewId = 1L, request = UpdateReviewRequest(1, "changed"))
            }.isInstanceOf(ReviewAccessDeniedException::class.java)

            assertThat(review.content).isEqualTo("x")
        }
    }

    @Nested
    @DisplayName("getProductReview - 상품별 리뷰 조회")
    inner class GetProductReviewTest {

        private val pageable = PageRequest.of(0, 10)
        private val request = ProductReviewSearchRequest(sortBy = ReviewSort.LATEST, rating = null)

        @Test
        fun 성공_리뷰어_이름_조합() {
            val now = LocalDateTime.now()
            val queryResults = listOf(
                ProductReviewQueryResult(1L, 10L, "리뷰1", 5, now, now),
                ProductReviewQueryResult(2L, 20L, "리뷰2", 4, now, now),
            )
            given(reviewRepository.getProductReviews(1L, request, pageable))
                .willReturn(PageImpl(queryResults, pageable, 2L))
            given(memberClient.getMemberInfos(listOf(10L, 20L)))
                .willReturn(listOf(MemberInfoResponse(10L, "김철수"), MemberInfoResponse(20L, "이영희")))

            val result = reviewService.getProductReview(1L, request, pageable)

            assertThat(result.content).hasSize(2)
            assertThat(result.content[0].memberName).isEqualTo("김철수")
            assertThat(result.content[1].memberName).isEqualTo("이영희")
            assertThat(result.totalElements).isEqualTo(2L)
        }

        @Test
        fun 리뷰_없으면_memberClient_호출_안함() {
            given(reviewRepository.getProductReviews(1L, request, pageable))
                .willReturn(PageImpl(emptyList(), pageable, 0L))

            val result = reviewService.getProductReview(1L, request, pageable)

            assertThat(result.content).isEmpty()
            verify(memberClient, never()).getMemberInfos(anyList())
        }
    }

    @Nested
    @DisplayName("getReviewDetail - 리뷰 상세 조회")
    inner class GetReviewDetailTest {

        @Test
        fun 성공() {
            val now = LocalDateTime.now()
            val review = Review(id = 1L, orderId = 10L, productId = 100L, memberId = 5L, rating = 4, content = "괜찮음").apply {
                createdAt = now
                updatedAt = now
            }
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))
            given(memberClient.getMemberInfos(listOf(5L)))
                .willReturn(listOf(MemberInfoResponse(5L, "홍길동")))

            val result = reviewService.getReviewDetail(1L)

            assertThat(result.id).isEqualTo(1L)
            assertThat(result.productId).isEqualTo(100L)
            assertThat(result.memberId).isEqualTo(5L)
            assertThat(result.memberName).isEqualTo("홍길동")
            assertThat(result.rating).isEqualTo(4)
            assertThat(result.content).isEqualTo("괜찮음")
        }

        @Test
        fun 실패_리뷰_없음() {
            given(reviewRepository.findById(1L)).willReturn(Optional.empty())

            assertThatThrownBy { reviewService.getReviewDetail(1L) }
                .isInstanceOf(ReviewNotFoundException::class.java)

            verify(memberClient, never()).getMemberInfos(anyList())
        }

        @Test
        fun member_service_실패시_memberName_null() {
            val now = LocalDateTime.now()
            val review = Review(id = 1L, orderId = 10L, productId = 100L, memberId = 5L, rating = 4, content = "x").apply {
                createdAt = now
                updatedAt = now
            }
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review))
            // fallback이 emptyList() 반환하는 상황 재현
            given(memberClient.getMemberInfos(listOf(5L))).willReturn(emptyList())

            val result = reviewService.getReviewDetail(1L)

            assertThat(result.memberName).isNull()
        }
    }

    @Nested
    @DisplayName("getMyReview - 내 리뷰 목록 조회")
    inner class GetMyReviewTest {

        private val pageable = PageRequest.of(0, 10)

        @Test
        fun 성공_상품명_조합() {
            val now = LocalDateTime.now()
            val queryResults = listOf(
                MyReviewQueryResult(1L, 100L, "리뷰1", 5, now, now),
                MyReviewQueryResult(2L, 200L, "리뷰2", 4, now, now),
            )
            given(reviewRepository.getMyReviews(1L, pageable))
                .willReturn(PageImpl(queryResults, pageable, 2L))
            given(productClient.getProductNames(listOf(100L, 200L)))
                .willReturn(ProductNameResponse(mapOf(100L to "상품A", 200L to "상품B")))

            val result = reviewService.getMyReview(memberId = 1L, pageable = pageable)

            assertThat(result.content).hasSize(2)
            assertThat(result.content[0].productName).isEqualTo("상품A")
            assertThat(result.content[1].productName).isEqualTo("상품B")
            assertThat(result.totalElements).isEqualTo(2L)
        }

        @Test
        fun 이름_못찾은_productId는_null() {
            val now = LocalDateTime.now()
            val queryResults = listOf(
                MyReviewQueryResult(1L, 100L, "리뷰1", 5, now, now),
            )
            given(reviewRepository.getMyReviews(1L, pageable))
                .willReturn(PageImpl(queryResults, pageable, 1L))
            given(productClient.getProductNames(listOf(100L)))
                .willReturn(ProductNameResponse(emptyMap()))

            val result = reviewService.getMyReview(1L, pageable)

            assertThat(result.content[0].productName).isNull()
        }
    }

}
