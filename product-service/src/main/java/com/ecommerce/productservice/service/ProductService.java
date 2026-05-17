package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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
}
