package com.ecommerce.productservice.domain.controller;

import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> getProductList(@ModelAttribute SearchRequest request, Pageable pageable) {
        return ResponseEntity.ok(productService.getProductList(request, pageable));
    }
}
