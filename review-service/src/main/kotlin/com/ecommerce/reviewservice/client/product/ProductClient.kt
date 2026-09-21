package com.ecommerce.reviewservice.client.product

import com.ecommerce.reviewservice.client.product.dto.ProductNameResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient("product-service", fallbackFactory = ProductClientFallbackFactory::class)
interface ProductClient {

    @GetMapping("/api/v1/product/names")
    fun getProductNames(@RequestParam productIds: List<Long>): ProductNameResponse
}
