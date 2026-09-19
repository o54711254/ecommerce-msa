package com.ecommerce.reviewservice.domain.controller

import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.dto.req.UpdateReviewRequest
import com.ecommerce.reviewservice.domain.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
    fun getProductReview(@PathVariable("productId") productId: Long): ResponseEntity<Boolean> {
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "내 리뷰 목록")
    @GetMapping("/my")
    fun getMyReviews(@RequestHeader("X-Member-Id") memberId: Long): ResponseEntity<Boolean> {
        return ResponseEntity.ok().build()
    }
}