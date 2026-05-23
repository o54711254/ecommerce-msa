package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {
    Page<ProductListResponse> getProductPage(SearchRequest request, Pageable pageable);
    Page<ProductListResponse> getMyProductPage(Long sellerId, SearchRequest request, Pageable pageable);
    Map<Long, Long> getPriceMap(List<Long> productIds);
    Map<Long, String> getNamesMap(List<Long> productIds);
}
