package com.ecommerce.reviewservice.domain.controller

import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.dto.req.ProductReviewSearchRequest
import com.ecommerce.reviewservice.domain.dto.req.UpdateReviewRequest
import com.ecommerce.reviewservice.domain.dto.res.MyReviewListResponse
import com.ecommerce.reviewservice.domain.dto.res.ProductReviewListResponse
import com.ecommerce.reviewservice.domain.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Review API")
@RequestMapping("/api/v1/review")
@RestController
class ReviewController(
    private val reviewService: ReviewService
) {
    @Operation(summary = "리뷰 등록")
    @PostMapping
    fun createReview(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody request: CreateReviewRequest
    ): ResponseEntity<Long> {
        return ResponseEntity.ok(reviewService.createReview(memberId, request))
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{id}")
    fun deleteReview(
        @RequestHeader("X-Member-Id") memberId: Long,
        @PathVariable("id") id: Long
    ): ResponseEntity<Boolean> {
        reviewService.deleteReview(memberId, id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "리뷰 수정")
    @PutMapping("/{id}")
    fun updateReview(
        @RequestHeader("X-Member-Id") memberId: Long,
        @PathVariable("id") id: Long,
        @RequestBody request: UpdateReviewRequest
    ): ResponseEntity<Long> {
        reviewService.updateReview(memberId, id, request)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("/{id}")
    fun getReviewDetail(@PathVariable("id") reviewId: Long): ResponseEntity<Boolean> {
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "상품별 리뷰 조회")
    @GetMapping("/product/{productId}")
    fun getProductReview(
        @PathVariable("productId") productId: Long,
        @ParameterObject @ModelAttribute request: ProductReviewSearchRequest,
        pageable: Pageable
    ): ResponseEntity<Page<ProductReviewListResponse>> {
        return ResponseEntity.ok(reviewService.getProductReview(productId, request, pageable))
    }

    @Operation(summary = "내 리뷰 목록")
    @GetMapping("/my")
    fun getMyReviews(
        @RequestHeader("X-Member-Id") memberId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<MyReviewListResponse>> {
        return ResponseEntity.ok(reviewService.getMyReview(memberId, pageable))
    }
}