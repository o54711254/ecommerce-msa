package com.ecommerce.reviewservice.domain.controller

import com.ecommerce.reviewservice.domain.dto.req.CreateReviewRequest
import com.ecommerce.reviewservice.domain.service.ReviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/review")
@RestController
class ReviewController(
    private val reviewService: ReviewService
) {
    @PostMapping
    fun createReview(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody request: CreateReviewRequest
    ): ResponseEntity<Long> {
        return ResponseEntity.ok(reviewService.createReview(memberId, request))
    }
}