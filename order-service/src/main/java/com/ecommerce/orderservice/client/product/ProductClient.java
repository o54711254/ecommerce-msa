package com.ecommerce.orderservice.client.product;

import com.ecommerce.orderservice.client.product.dto.ProductNameResponse;
import com.ecommerce.orderservice.client.product.dto.ProductPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @GetMapping("/api/v1/product/price")
    ProductPriceResponse getPriceMap(@RequestParam List<Long> productIds);

    @GetMapping("/api/v1/product/names")
    ProductNameResponse getNamesMap(@RequestParam List<Long> productIds);
}
