package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ecommerce.productservice.domain.entity.QProduct.product;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductListResponse> getProductList(SearchRequest request, Pageable pageable) {
        List<ProductListResponse> list = getProducts(request, pageable);
        long count = getCount(request);
        return new PageImpl<>(list, pageable, count);
    }

    private Long getCount(SearchRequest request) {
        return jpaQueryFactory.select(product.count())
                .from(product)
                .where(
                        nameContains(request)
                )
                .fetchOne();
    }

    private List<ProductListResponse> getProducts(SearchRequest request, Pageable pageable) {
        return jpaQueryFactory.select(Projections.constructor(ProductListResponse.class,
                        product.name.as("productName"),
                        product.price.as("price"),
                        product.status.as("status")
                ))
                .from(product)
                .where(
                        nameContains(request)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression nameContains(SearchRequest request) {
        String searchInput = request.searchInput();
        if (searchInput == null || searchInput.isEmpty()) {
            return null;
        }
        return product.name.contains(searchInput);
    }
}
