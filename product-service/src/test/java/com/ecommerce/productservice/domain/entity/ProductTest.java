package com.ecommerce.productservice.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Nested
    @DisplayName("create - 상품 생성")
    class CreateTest {

        @Test
        void 성공() {
            Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);

            assertThat(product.getSellerId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("사과");
            assertThat(product.getDescription()).isEqualTo("맛있는 사과");
            assertThat(product.getPrice()).isEqualTo(1000L);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("update - 상품 수정")
    class UpdateTest {

        @Test
        void 성공() {
            Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);

            product.update("수정된 사과", "더 맛있는 사과", 2000L);

            assertThat(product.getName()).isEqualTo("수정된 사과");
            assertThat(product.getDescription()).isEqualTo("더 맛있는 사과");
            assertThat(product.getPrice()).isEqualTo(2000L);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        }
    }
}
