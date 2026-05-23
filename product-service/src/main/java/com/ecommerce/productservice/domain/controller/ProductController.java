package com.ecommerce.productservice.domain.controller;

import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductDetailResponse;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.dto.res.ProductPriceResponse;
import com.ecommerce.productservice.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.ok(productService.getProductPage(request, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ProductListResponse>> getMyProductList(@RequestHeader("X-Member-Id") Long sellerId,
                                                                      @RequestHeader("X-Member-Role") String role,
                                                                      @ModelAttribute SearchRequest request, Pageable pageable) {
        return ResponseEntity.ok(productService.getMyProductPage(sellerId, role, request, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateProduct(@PathVariable Long id,
                                              @RequestHeader("X-Member-Id") Long sellerId,
                                              @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, sellerId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id,
                                           @RequestHeader("X-Member-Id") Long sellerId) {
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/price")
    public ResponseEntity<ProductPriceResponse> getPriceMap(@RequestParam List<Long> productIds) {
        return ResponseEntity.ok(productService.getPriceMap(productIds));
    }
}
