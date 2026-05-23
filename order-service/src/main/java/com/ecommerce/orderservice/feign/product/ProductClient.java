package com.ecommerce.orderservice.feign.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/product/price")
    ResponseEntity<ProductPriceResponse> getPriceMap(@RequestParam List<Long> productIds);
}
