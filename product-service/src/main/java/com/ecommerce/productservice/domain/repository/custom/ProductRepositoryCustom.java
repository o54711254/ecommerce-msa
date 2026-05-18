package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductListResponse> getProductList(SearchRequest request, Pageable pageable);
}
