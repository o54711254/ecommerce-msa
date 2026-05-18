package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductListResponse> getProductPage(SearchRequest request, Pageable pageable);
    Page<ProductListResponse> getMyProductPage(Long sellerId, SearchRequest request, Pageable pageable);
}
