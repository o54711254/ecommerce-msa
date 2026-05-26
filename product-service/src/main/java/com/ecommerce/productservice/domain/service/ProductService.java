package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.client.inventory.InventoryClient;
import com.ecommerce.productservice.client.inventory.dto.req.CreateInventoryRequest;
import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import com.ecommerce.productservice.client.member.MemberClient;
import com.ecommerce.productservice.client.member.dto.SellerResponse;
import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductDetailResponse;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.dto.res.ProductNameResponse;
import com.ecommerce.productservice.domain.dto.res.ProductPriceResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.repository.ProductRepository;
import com.ecommerce.productservice.global.exception.custom.ForbiddenException;
import com.ecommerce.productservice.global.exception.custom.ProductAccessDeniedException;
import com.ecommerce.productservice.global.exception.custom.ProductNotFoundException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private final MemberClient memberClient;

    @Retry(name = "inventory-service")
    @Transactional
    public Long createProduct(Long sellerId, String role, CreateProductRequest request) {
        if (!role.equals("SELLER")) {
            throw new ForbiddenException();
        }

        Product product = Product.create(sellerId, request.name(), request.description(), request.price());
        Long productId = productRepository.save(product).getId();

        inventoryClient.createInventory(new CreateInventoryRequest(productId, request.quantity()));

        return productId;
    }

    @Retry(name = "inventory-service")
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long id) {
        Product product = getProduct(id);

        InventoryResponse inventory = inventoryClient.getInventory(product.getId()).getBody();
        SellerResponse seller = memberClient.getSeller(product.getSellerId()).getBody();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .createdDate(product.getCreatedAt().toLocalDate())
                .sellerId(product.getSellerId())
                .sellerName(seller.name())
                .sellerEmail(seller.email())
                .quantity(inventory.quantity())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProductPage(SearchRequest request, Pageable pageable) {
        return productRepository.getProductPage(request, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getMyProductPage(Long sellerId, String role, SearchRequest request, Pageable pageable) {
        if (!role.equals("SELLER")) {
            throw new ForbiddenException();
        }

        return productRepository.getMyProductPage(sellerId, request, pageable);
    }

    @Transactional
    public Long updateProduct(Long id, Long sellerId, CreateProductRequest request) {
        Product product = getProduct(id);
        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        product.update(request.name(), request.description(), request.price());
        return product.getId();
    }

    @Retry(name = "inventory-service")
    @Transactional
    public void deleteProduct(Long id, Long sellerId) {
        Product product = getProduct(id);
        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductAccessDeniedException();
        }
        productRepository.delete(product);
        inventoryClient.deleteInventory(id);
    }

    @Transactional(readOnly = true)
    public ProductPriceResponse getPriceMap(List<Long> productIds) {
        return new ProductPriceResponse(productRepository.getPriceMap(productIds));
    }

    @Transactional(readOnly = true)
    public ProductNameResponse getNamesMap(List<Long> productIds) {
        return new ProductNameResponse(productRepository.getNamesMap(productIds));
    }


    private Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }
}
