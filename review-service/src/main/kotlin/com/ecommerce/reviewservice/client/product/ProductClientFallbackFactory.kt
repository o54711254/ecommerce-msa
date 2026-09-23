package com.ecommerce.reviewservice.client.product

import com.ecommerce.reviewservice.client.product.dto.ProductNameResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class ProductClientFallbackFactory : FallbackFactory<ProductClient> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun create(cause: Throwable?): ProductClient {
        log.error("product-service 호출 실패 - {}", cause?.message)
        return object : ProductClient {
            override fun getProductNames(productIds: List<Long>): ProductNameResponse {
                return ProductNameResponse(emptyMap())
            }
        }
    }
}
