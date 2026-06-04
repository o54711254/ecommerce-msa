package com.ecommerce.productservice.domain.repository.custom;

import com.ecommerce.productservice.AbstractIntegrationTest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ProductRepository productRepository;

    @Nested
    @Transactional
    @DisplayName("getProductPage - 상품 목록 반환")
    class GetProductPageTest {

        @Test
        void 검색어가_포함된_상품만_조회() {
            productRepository.save(Product.create(1L, "맥북 프로", "설명", 1000L));
            productRepository.save(Product.create(1L, "아이폰", "설명", 2000L));
            productRepository.save(Product.create(1L, "맥미니", "설명", 3000L));

            Page<ProductListResponse> result = productRepository.getProductPage(new SearchRequest("맥"), PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(ProductListResponse::productName)
                    .containsExactlyInAnyOrder("맥북 프로", "맥미니");
        }

        @Test
        void 검색어가_없으면_전체_상품_조회() {
            productRepository.save(Product.create(1L, "맥북 프로", "설명", 1000L));
            productRepository.save(Product.create(1L, "아이폰", "설명", 2000L));
            productRepository.save(Product.create(1L, "맥미니", "설명", 3000L));

            Page<ProductListResponse> result = productRepository.getProductPage(new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent())
                    .extracting(ProductListResponse::productName)
                    .containsExactlyInAnyOrder("맥북 프로", "아이폰", "맥미니");
        }

        @Test
        void 상품이_없으면_빈_목록_반환() {
            Page<ProductListResponse> result = productRepository.getProductPage(new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void 페이지_크기만큼만_반환() {
            productRepository.save(Product.create(1L, "상품1", "설명", 1000L));
            productRepository.save(Product.create(1L, "상품2", "설명", 2000L));
            productRepository.save(Product.create(1L, "상품3", "설명", 3000L));

            Pageable pageable = PageRequest.of(0, 2);
            Page<ProductListResponse> result = productRepository.getProductPage(new SearchRequest(null), pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @Transactional
    @DisplayName("getMyProductPage - 판매자 상품 목록 반환")
    class GetMyProductPageTest {

        @Test
        void 판매자_본인_상품만_조회() {
            productRepository.save(Product.create(1L, "맥북 프로", "설명", 1000L));
            productRepository.save(Product.create(1L, "아이폰", "설명", 2000L));
            productRepository.save(Product.create(2L, "갤럭시", "설명", 1500L));

            Page<ProductListResponse> result = productRepository.getMyProductPage(1L, new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(ProductListResponse::productName)
                    .containsExactlyInAnyOrder("맥북 프로", "아이폰");
        }

        @Test
        void 검색어와_판매자_ID_함께_필터링() {
            productRepository.save(Product.create(1L, "맥북 프로", "설명", 1000L));
            productRepository.save(Product.create(1L, "아이폰", "설명", 2000L));
            productRepository.save(Product.create(2L, "맥미니", "설명", 3000L)); // 다른 판매자

            Page<ProductListResponse> result = productRepository.getMyProductPage(1L, new SearchRequest("맥"), PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent())
                    .extracting(ProductListResponse::productName)
                    .containsExactly("맥북 프로");
        }

        @Test
        void 상품이_없으면_빈_목록_반환() {
            Page<ProductListResponse> result = productRepository.getMyProductPage(1L, new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getPriceMap - 상품 가격 일괄 조회")
    class GetPriceMapTest {

        @Test
        void 요청한_ID_목록의_가격_맵_반환() {
            Product p1 = productRepository.save(Product.create(1L, "맥북 프로", "설명", 1_000_000L));
            Product p2 = productRepository.save(Product.create(1L, "아이폰", "설명", 500_000L));
            Product p3 = productRepository.save(Product.create(1L, "맥미니", "설명", 800_000L));

            Map<Long, Long> result = productRepository.getPriceMap(List.of(p1.getId(), p2.getId()));

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry(p1.getId(), 1_000_000L)
                              .containsEntry(p2.getId(), 500_000L)
                              .doesNotContainKey(p3.getId());
        }

        @Test
        void 존재하지_않는_ID는_결과에_포함되지_않음() {
            Map<Long, Long> result = productRepository.getPriceMap(List.of(999L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getNamesMap - 상품명 일괄 조회")
    class GetNamesMapTest {

        @Test
        void 요청한_ID_목록의_이름_맵_반환() {
            Product p1 = productRepository.save(Product.create(1L, "맥북 프로", "설명", 1000L));
            Product p2 = productRepository.save(Product.create(1L, "아이폰", "설명", 2000L));
            Product p3 = productRepository.save(Product.create(1L, "맥미니", "설명", 3000L));

            Map<Long, String> result = productRepository.getNamesMap(List.of(p1.getId(), p2.getId()));

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry(p1.getId(), "맥북 프로")
                              .containsEntry(p2.getId(), "아이폰")
                              .doesNotContainKey(p3.getId());
        }

        @Test
        void 존재하지_않는_ID는_결과에_포함되지_않음() {
            Map<Long, String> result = productRepository.getNamesMap(List.of(999L));

            assertThat(result).isEmpty();
        }
    }
}
