package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.AbstractIntegrationTest;
import com.ecommerce.productservice.client.inventory.InventoryClient;
import com.ecommerce.productservice.client.inventory.InventoryClientHelper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class ProductServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;

    @MockitoBean private InventoryClient inventoryClient;
    @MockitoBean private InventoryClientHelper inventoryClientHelper;
    @MockitoBean private MemberClient memberClient;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("createProduct - 상품 등록")
    class CreateProductTest {

        @Test
        void 성공() {
            given(inventoryClient.createInventory(any())).willReturn(ResponseEntity.ok(1L));
            CreateProductRequest request = new CreateProductRequest("사과", "맛있는 사과", 1000L, 10);

            Long productId = productService.createProduct(1L, "SELLER", request);

            assertThat(productRepository.findById(productId)).isPresent();
            verify(inventoryClient).createInventory(any(CreateInventoryRequest.class));
        }

        @Test
        void 실패_권한없음() {
            assertThatThrownBy(() -> productService.createProduct(1L, "MEMBER", new CreateProductRequest("사과", "맛있는 사과", 1000L, 10)))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("getProductDetail - 상품 단건 조회")
    class GetProductDetailTest {

        @Test
        void 성공() {
            Product saved = productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));
            given(inventoryClientHelper.getInventory(saved.getId())).willReturn(new InventoryResponse(saved.getId(), 10));
            given(memberClient.getSeller(1L)).willReturn(ResponseEntity.ok(new SellerResponse("판매자", "seller@test.com")));

            ProductDetailResponse result = productService.getProductDetail(saved.getId());

            assertThat(result.getId()).isEqualTo(saved.getId());
            assertThat(result.getName()).isEqualTo("사과");
            assertThat(result.getQuantity()).isEqualTo(10);
            assertThat(result.getSellerName()).isEqualTo("판매자");
        }

        @Test
        void 실패_상품없음() {
            assertThatThrownBy(() -> productService.getProductDetail(999L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProductPage - 상품 목록 조회")
    class GetProductPageTest {

        @Test
        void 성공() {
            productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));
            productRepository.save(Product.create(1L, "배", "맛있는 배", 2000L));

            Page<ProductListResponse> result = productService.getProductPage(new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        void 성공_검색어_필터링() {
            productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));
            productRepository.save(Product.create(1L, "배", "맛있는 배", 2000L));

            Page<ProductListResponse> result = productService.getProductPage(new SearchRequest("사과"), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
        }
    }

    @Nested
    @DisplayName("getMyProductPage - 내 상품 목록 조회")
    class GetMyProductPageTest {

        @Test
        void 성공() {
            productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));
            productRepository.save(Product.create(2L, "배", "맛있는 배", 2000L));

            Page<ProductListResponse> result = productService.getMyProductPage(1L, "SELLER", new SearchRequest(null), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        void 실패_권한없음() {
            assertThatThrownBy(() -> productService.getMyProductPage(1L, "MEMBER", new SearchRequest(null), PageRequest.of(0, 10)))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("updateProduct - 상품 수정")
    class UpdateProductTest {

        @Test
        void 성공() {
            Product saved = productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));

            productService.updateProduct(saved.getId(), 1L, new CreateProductRequest("배", "맛있는 배", 2000L, 5));

            Product updated = productRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("배");
            assertThat(updated.getPrice()).isEqualTo(2000L);
        }

        @Test
        void 실패_상품없음() {
            assertThatThrownBy(() -> productService.updateProduct(999L, 1L, new CreateProductRequest("배", "맛있는 배", 2000L, 5)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void 실패_본인_상품_아님() {
            Product saved = productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));

            assertThatThrownBy(() -> productService.updateProduct(saved.getId(), 2L, new CreateProductRequest("배", "맛있는 배", 2000L, 5)))
                    .isInstanceOf(ProductAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deleteProduct - 상품 삭제")
    class DeleteProductTest {

        @Test
        void 성공() {
            Product saved = productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));
            given(inventoryClient.deleteInventory(saved.getId())).willReturn(ResponseEntity.ok().build());

            productService.deleteProduct(saved.getId(), 1L);

            assertThat(productRepository.findById(saved.getId())).isEmpty();
            verify(inventoryClient).deleteInventory(saved.getId());
        }

        @Test
        void 실패_상품없음() {
            assertThatThrownBy(() -> productService.deleteProduct(999L, 1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void 실패_본인_상품_아님() {
            Product saved = productRepository.save(Product.create(1L, "사과", "맛있는 사과", 1000L));

            assertThatThrownBy(() -> productService.deleteProduct(saved.getId(), 2L))
                    .isInstanceOf(ProductAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getPriceMap - 상품 가격 일괄 조회")
    class GetPriceMapTest {

        @Test
        void 성공() {
            Product p1 = productRepository.save(Product.create(1L, "사과", "사과", 1000L));
            Product p2 = productRepository.save(Product.create(1L, "배", "배", 2000L));

            ProductPriceResponse result = productService.getPriceMap(List.of(p1.getId(), p2.getId()));

            assertThat(result.priceMap()).containsEntry(p1.getId(), 1000L);
            assertThat(result.priceMap()).containsEntry(p2.getId(), 2000L);
        }
    }

    @Nested
    @DisplayName("getNamesMap - 상품명 일괄 조회")
    class GetNamesMapTest {

        @Test
        void 성공() {
            Product p1 = productRepository.save(Product.create(1L, "사과", "사과", 1000L));
            Product p2 = productRepository.save(Product.create(1L, "배", "배", 2000L));

            ProductNameResponse result = productService.getNamesMap(List.of(p1.getId(), p2.getId()));

            assertThat(result.nameMap()).containsEntry(p1.getId(), "사과");
            assertThat(result.nameMap()).containsEntry(p2.getId(), "배");
        }
    }
}
