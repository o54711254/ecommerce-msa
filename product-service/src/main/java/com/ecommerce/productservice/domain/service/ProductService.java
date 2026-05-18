package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.repository.ProductRepository;
import com.ecommerce.productservice.infra.feign.CreateInventoryRequest;
import com.ecommerce.productservice.infra.feign.InventoryClient;
import jakarta.persistence.EntityNotFoundException;
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
    private final InventoryClient inventoryClient;

    @Transactional
    public Long createProduct(Long sellerId, String role, CreateProductRequest request) {
        if (!role.equals("SELLER")) {
            throw new InvalidParameterException("Invalid role");
        }

        Product product = Product.create(sellerId, request.name(), request.description(), request.price());
        Long productId = productRepository.save(product).getId();

        inventoryClient.createInventory(new CreateInventoryRequest(productId, request.quantity()));

        return productId;
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProductPage(SearchRequest request, Pageable pageable) {
        return productRepository.getProductPage(request, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getMyProductPage(Long sellerId, String role, SearchRequest request, Pageable pageable) {
        if (!role.equals("SELLER")) {
            throw new InvalidParameterException("Invalid role");
        }

        return productRepository.getMyProductPage(sellerId, request, pageable);
    }

    @Transactional
    public Long updateProduct(Long id, Long sellerId, CreateProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!product.getSellerId().equals(sellerId)) {
            throw new InvalidParameterException("Invalid sellerId");
        }

        product.update(request.name(), request.description(), request.price());
        return product.getId();
    }

    @Transactional
    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!product.getSellerId().equals(sellerId)) {
            throw new InvalidParameterException("Invalid sellerId");
        }
        productRepository.delete(product);
    }
}
