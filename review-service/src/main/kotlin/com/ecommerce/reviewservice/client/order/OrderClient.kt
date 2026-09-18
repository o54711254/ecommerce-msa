package com.ecommerce.reviewservice.client.order

import com.ecommerce.reviewservice.client.order.dto.res.OrderResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient("order-service", fallbackFactory = OrderClientFallbackFactory::class)
interface OrderClient {

    @GetMapping("/api/v1/order/{orderId}")
    fun getOrder(
        @RequestHeader("X-Member-Id") memberId: Long,
        @PathVariable orderId: Long
    ): OrderResponse
}