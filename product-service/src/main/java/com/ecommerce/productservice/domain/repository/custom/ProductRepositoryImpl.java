package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ecommerce.productservice.domain.entity.QProduct.product;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProductListResponse> getProductPage(SearchRequest request, Pageable pageable) {
        List<ProductListResponse> list = getProducts(null, request, pageable);
        long count = getCount(null, request);
        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public Page<ProductListResponse> getMyProductPage(Long sellerId, SearchRequest request, Pageable pageable) {
        List<ProductListResponse> list = getProducts(sellerId, request, pageable);
        long count = getCount(sellerId, request);
        return new PageImpl<>(list, pageable, count);
    }

    private Long getCount(Long sellerId, SearchRequest request) {
        return jpaQueryFactory.select(product.count())
                .from(product)
                .where(
                        whereQuery(sellerId, request)
                )
                .fetchOne();
    }

    private List<ProductListResponse> getProducts(Long sellerId, SearchRequest request, Pageable pageable) {
        return jpaQueryFactory.select(Projections.constructor(ProductListResponse.class,
                        product.name.as("productName"),
                        product.price.as("price"),
                        product.status.as("status")
                ))
                .from(product)
                .where(
                        whereQuery(sellerId, request)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanBuilder whereQuery(Long sellerId, SearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(eqSellerId(sellerId));
        builder.and(nameContains(request));
        return builder;
    }

    @Override
    public Map<Long, Long> getPriceMap(List<Long> productIds) {
        return jpaQueryFactory.select(product.id, product.price)
                .from(product)
                .where(product.id.in(productIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(product.id),
                        tuple -> tuple.get(product.price)
                ));
    }

    private BooleanExpression eqSellerId(Long sellerId) {
        return sellerId == null ? null : product.sellerId.eq(sellerId);
    }

    private BooleanExpression nameContains(SearchRequest request) {
        String searchInput = request.searchInput();
        if (searchInput == null || searchInput.isEmpty()) {
            return null;
        }
        return product.name.contains(searchInput);
    }
}
