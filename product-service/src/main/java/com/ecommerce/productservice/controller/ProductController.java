package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Long> createProduct(@RequestHeader("X-Member-Id") Long sellerId,
                                              @RequestHeader("X-Member-Role") String role,
                                              @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(sellerId, role, request));
    }
}
