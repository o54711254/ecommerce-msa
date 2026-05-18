package com.ecommerce.productservice.domain.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProductDetailResponse {

    /* 상품 정보 */
    private Long id;
    private String name;
    private String description;
    private Long price;
    private LocalDate createdDate;

    /* 판매자 정보 */
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;

    /* 재고 정보 */
    private Integer quantity;
}
