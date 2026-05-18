package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Long createProduct(Long sellerId, String role, CreateProductRequest request) {
        if (!role.equals("SELLER")) {
            throw new InvalidParameterException("Invalid role");
        }

        Product product = Product.create(sellerId, request.name(), request.description(), request.price());

        return productRepository.save(product).getId();
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProductList(SearchRequest request, Pageable pageable) {
        return productRepository.getProductList(request, pageable);
    }
}
