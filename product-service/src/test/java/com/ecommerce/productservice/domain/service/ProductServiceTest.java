package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import com.ecommerce.productservice.client.member.dto.SellerResponse;
import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductDetailResponse;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.entity.ProductStatus;
import com.ecommerce.productservice.domain.repository.ProductRepository;
import com.ecommerce.productservice.client.inventory.InventoryClient;
import com.ecommerce.productservice.client.inventory.InventoryClientHelper;
import com.ecommerce.productservice.client.member.MemberClient;
import com.ecommerce.productservice.global.exception.custom.ForbiddenException;
import com.ecommerce.productservice.global.exception.custom.ProductAccessDeniedException;
import com.ecommerce.productservice.global.exception.custom.ProductNotFoundException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private InventoryClient inventoryClient;
    @Mock private InventoryClientHelper inventoryClientHelper;
    @Mock private MemberClient memberClient;
    @InjectMocks private ProductService productService;

    @Nested
    @DisplayName("createProduct - 상품 등록")
    class CreateProductTest {

        @Test
        void 성공() {
            Long sellerId = 1L;
            CreateProductRequest request = new CreateProductRequest("사과", "맛있는 사과", 1000L, 10);
            Product savedProduct = Product.create(sellerId, request.name(), request.description(), request.price());
            ReflectionTestUtils.setField(savedProduct, "id", 1L);
            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            Long result = productService.createProduct(sellerId, "SELLER", request);

            assertThat(result).isEqualTo(1L);
            verify(productRepository).save(any(Product.class));
            verify(inventoryClient).createInventory(any());
        }

        @Test
        void 실패_SELLER_권한_없음() {
            CreateProductRequest request = new CreateProductRequest("사과", "맛있는 사과", 1000L, 10);

            assertThatThrownBy(() -> productService.createProduct(1L, "MEMBER", request))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("getProductPage - 상품 목록 조회")
    class GetProductPageTest {

        @Test
        void 성공() {
            SearchRequest request = new SearchRequest(null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProductListResponse> expected = new PageImpl<>(List.of(
                    new ProductListResponse("사과", 1000L, ProductStatus.AVAILABLE)
            ));
            given(productRepository.getProductPage(request, pageable)).willReturn(expected);

            Page<ProductListResponse> result = productService.getProductPage(request, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
        }
    }

    @Nested
    @DisplayName("getMyProductPage - 내 상품 목록 조회")
    class GetMyProductPageTest {

        @Test
        void 성공() {
            Long sellerId = 1L;
            SearchRequest request = new SearchRequest(null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProductListResponse> expected = new PageImpl<>(List.of(
                    new ProductListResponse("사과", 1000L, ProductStatus.AVAILABLE)
            ));
            given(productRepository.getMyProductPage(sellerId, request, pageable)).willReturn(expected);

            Page<ProductListResponse> result = productService.getMyProductPage(sellerId, "SELLER", request, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
        }

        @Test
        void 실패_SELLER_권한_없음() {
            SearchRequest request = new SearchRequest(null);
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> productService.getMyProductPage(1L, "MEMBER", request, pageable))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("getProductDetail - 상품 상세 조회")
    class GetProductDetailTest {

        @Test
        void 성공() {
            Long productId = 1L;
            Long sellerId = 1L;
            Product product = Product.create(sellerId, "사과", "맛있는 사과", 1000L);
            ReflectionTestUtils.setField(product, "id", productId);
            ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2024, 1, 1, 0, 0));
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(inventoryClientHelper.getInventory(productId)).willReturn(new InventoryResponse(productId, 10));
            given(memberClient.getSeller(sellerId)).willReturn(new SellerResponse("판매자", "seller@test.com"));

            ProductDetailResponse result = productService.getProductDetail(productId);

            assertThat(result.getName()).isEqualTo("사과");
            assertThat(result.getPrice()).isEqualTo(1000L);
            assertThat(result.getQuantity()).isEqualTo(10);
            assertThat(result.getSellerName()).isEqualTo("판매자");
            assertThat(result.getSellerEmail()).isEqualTo("seller@test.com");
        }

        @Test
        void 실패_상품_없음() {
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductDetail(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProduct - 상품 수정")
    class UpdateProductTest {

        @Test
        void 성공() {
            Long sellerId = 1L;
            Product product = Product.create(sellerId, "사과", "맛있는 사과", 1000L);
            ReflectionTestUtils.setField(product, "id", 1L);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            Long result = productService.updateProduct(1L, sellerId, new CreateProductRequest("수정된 사과", "더 맛있는 사과", 2000L, null));

            assertThat(result).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("수정된 사과");
            assertThat(product.getPrice()).isEqualTo(2000L);
        }

        @Test
        void 실패_본인_상품_아님() {
            Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.updateProduct(1L, 2L, new CreateProductRequest("수정", "수정", 2000L, null)))
                    .isInstanceOf(ProductAccessDeniedException.class);
        }

        @Test
        void 실패_상품_없음() {
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(1L, 1L, new CreateProductRequest("수정", "수정", 2000L, null)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteProduct - 상품 삭제")
    class DeleteProductTest {

        @Test
        void 성공() {
            Long sellerId = 1L;
            Product product = Product.create(sellerId, "사과", "맛있는 사과", 1000L);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            productService.deleteProduct(1L, sellerId);

            verify(productRepository).delete(product);
            verify(inventoryClient).deleteInventory(1L);
        }

        @Test
        void 실패_본인_상품_아님() {
            Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.deleteProduct(1L, 2L))
                    .isInstanceOf(ProductAccessDeniedException.class);
        }

        @Test
        void 실패_상품_없음() {
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
