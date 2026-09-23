package com.ecommerce.reviewservice.client.order

import com.ecommerce.reviewservice.client.order.dto.res.OrderResponse
import com.ecommerce.reviewservice.global.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class OrderClientFallbackFactory : FallbackFactory<OrderClient> {

    private val log = LoggerFactory.getLogger(OrderClientFallbackFactory::class.java)

    override fun create(cause: Throwable?): OrderClient {
        log.error("order-service 호출 실패 - {}", cause?.message)
        return object : OrderClient {
            override fun getOrder(memberId: Long, orderId: Long): OrderResponse {
                throw ExternalServiceException("order-service", cause)
            }
        }
    }
}