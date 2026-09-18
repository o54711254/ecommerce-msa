package com.ecommerce.reviewservice.domain.repository

import com.ecommerce.reviewservice.domain.entity.Review
import com.ecommerce.reviewservice.domain.repository.custom.ReviewRepositoryCustom
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long>, ReviewRepositoryCustom {

    fun existsByOrderIdAndProductId(orderId: Long, productId: Long): Boolean
}