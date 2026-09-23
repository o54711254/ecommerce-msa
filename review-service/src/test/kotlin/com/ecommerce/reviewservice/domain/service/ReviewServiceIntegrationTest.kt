package com.ecommerce.reviewservice.domain.service

import com.ecommerce.reviewservice.AbstractIntegrationTest
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
import com.ecommerce.reviewservice.domain.entity.Review
import com.ecommerce.reviewservice.domain.repository.ReviewRepository
import com.ecommerce.reviewservice.global.exception.custom.ReviewNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime

class ReviewServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired private lateinit var reviewService: ReviewService
    @Autowired private lateinit var reviewRepository: ReviewRepository

    @MockitoBean private lateinit var orderClient: OrderClient
    @MockitoBean private lateinit var memberClient: MemberClient
    @MockitoBean private lateinit var productClient: ProductClient

    @BeforeEach
    fun setUp() {
        reviewRepository.deleteAll()
    }

    @Nested
    @DisplayName("createReview - 리뷰 생성")
    inner class CreateReviewTest {

        @Test
        fun 성공_리뷰가_DB에_저장() {
            val memberId = 1L
            val request = CreateReviewRequest(orderId = 10L, productId = 100L, rating = 5, content = "좋아요")
            given(orderClient.getOrder(memberId, 10L)).willReturn(
                OrderResponse(
                    orderId = 10L,
                    totalPrice = 5000L,
                    orderStatus = OrderStatus.PAID,
                    createdAt = LocalDateTime.now(),
                    itemList = listOf(OrderItemResponse(1L, 100L, "상품", 1, 5000L, 5000L)),
                )
            )

            val reviewId = reviewService.createReview(memberId, request)

            val saved = reviewRepository.findById(reviewId).orElseThrow()
            assertThat(saved.memberId).isEqualTo(memberId)
            assertThat(saved.orderId).isEqualTo(10L)
            assertThat(saved.productId).isEqualTo(100L)
            assertThat(saved.rating).isEqualTo(5)
            assertThat(saved.content).isEqualTo("좋아요")
        }
    }

    @Nested
    @DisplayName("deleteReview - 리뷰 삭제")
    inner class DeleteReviewTest {

        @Test
        fun 성공_리뷰가_DB에서_삭제() {
            val saved = reviewRepository.save(
                Review.create(orderId = 10L, productId = 100L, memberId = 1L, rating = 5, content = "hi")
            )

            reviewService.deleteReview(memberId = 1L, reviewId = saved.id!!)

            assertThat(reviewRepository.findById(saved.id!!)).isEmpty
        }
    }

    @Nested
    @DisplayName("updateReview - 리뷰 수정")
    inner class UpdateReviewTest {

        @Test
        fun 성공_content와_rating_반영() {
            val saved = reviewRepository.save(
                Review.create(orderId = 10L, productId = 100L, memberId = 1L, rating = 3, content = "old")
            )

            reviewService.updateReview(1L, saved.id!!, UpdateReviewRequest(rating = 5, content = "new"))

            val updated = reviewRepository.findById(saved.id!!).orElseThrow()
            assertThat(updated.rating).isEqualTo(5)
            assertThat(updated.content).isEqualTo("new")
        }
    }

    @Nested
    @DisplayName("getProductReview - 상품별 리뷰 조회 (QueryDSL)")
    inner class GetProductReviewTest {

        @Test
        fun 성공_페이징() {
            (1..15).forEach { i ->
                reviewRepository.save(
                    Review.create(orderId = i.toLong(), productId = 100L, memberId = i.toLong(), rating = 5, content = "리뷰$i")
                )
            }
            given(memberClient.getMemberInfos(anyList())).willReturn(emptyList())

            val result = reviewService.getProductReview(
                productId = 100L,
                request = ProductReviewSearchRequest(sortBy = ReviewSort.LATEST, rating = null),
                pageable = PageRequest.of(0, 10),
            )

            assertThat(result.content).hasSize(10)
            assertThat(result.totalElements).isEqualTo(15)
        }

        @Test
        fun 성공_별점_필터() {
            reviewRepository.save(Review.create(orderId = 1L, productId = 100L, memberId = 1L, rating = 5, content = "a"))
            reviewRepository.save(Review.create(orderId = 2L, productId = 100L, memberId = 2L, rating = 3, content = "b"))
            reviewRepository.save(Review.create(orderId = 3L, productId = 100L, memberId = 3L, rating = 5, content = "c"))
            given(memberClient.getMemberInfos(anyList())).willReturn(emptyList())

            val result = reviewService.getProductReview(
                productId = 100L,
                request = ProductReviewSearchRequest(sortBy = ReviewSort.LATEST, rating = 5),
                pageable = PageRequest.of(0, 10),
            )

            assertThat(result.content).hasSize(2)
            assertThat(result.content).allSatisfy { assertThat(it.rating).isEqualTo(5) }
        }

        @Test
        fun 성공_다른_상품_리뷰는_제외() {
            reviewRepository.save(Review.create(orderId = 1L, productId = 100L, memberId = 1L, rating = 5, content = "대상"))
            reviewRepository.save(Review.create(orderId = 2L, productId = 200L, memberId = 2L, rating = 5, content = "다른상품"))
            given(memberClient.getMemberInfos(anyList())).willReturn(emptyList())

            val result = reviewService.getProductReview(
                productId = 100L,
                request = ProductReviewSearchRequest(sortBy = ReviewSort.LATEST, rating = null),
                pageable = PageRequest.of(0, 10),
            )

            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].content).isEqualTo("대상")
        }
    }

    @Nested
    @DisplayName("getMyReview - 내 리뷰 목록 조회 (QueryDSL)")
    inner class GetMyReviewTest {

        @Test
        fun 성공_본인_리뷰만_조회() {
            reviewRepository.save(Review.create(orderId = 1L, productId = 100L, memberId = 1L, rating = 5, content = "내리뷰"))
            reviewRepository.save(Review.create(orderId = 2L, productId = 200L, memberId = 2L, rating = 4, content = "남의리뷰"))
            given(productClient.getProductNames(anyList()))
                .willReturn(ProductNameResponse(mapOf(100L to "상품A")))

            val result = reviewService.getMyReview(memberId = 1L, pageable = PageRequest.of(0, 10))

            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].productName).isEqualTo("상품A")
            assertThat(result.content[0].content).isEqualTo("내리뷰")
        }
    }

    @Nested
    @DisplayName("getReviewDetail - 리뷰 상세 조회")
    inner class GetReviewDetailTest {

        @Test
        fun 성공() {
            val saved = reviewRepository.save(
                Review.create(orderId = 10L, productId = 100L, memberId = 1L, rating = 5, content = "hi")
            )
            given(memberClient.getMemberInfos(listOf(1L)))
                .willReturn(listOf(MemberInfoResponse(1L, "홍길동")))

            val result = reviewService.getReviewDetail(saved.id!!)

            assertThat(result.id).isEqualTo(saved.id)
            assertThat(result.memberName).isEqualTo("홍길동")
            assertThat(result.content).isEqualTo("hi")
        }

        @Test
        fun 실패_리뷰_없음() {
            assertThatThrownBy { reviewService.getReviewDetail(999L) }
                .isInstanceOf(ReviewNotFoundException::class.java)
        }
    }
}
