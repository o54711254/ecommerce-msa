package com.ecommerce.productservice.domain.service;

import com.ecommerce.productservice.domain.dto.req.CreateProductRequest;
import com.ecommerce.productservice.domain.dto.req.SearchRequest;
import com.ecommerce.productservice.domain.dto.res.ProductListResponse;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.entity.ProductStatus;
import com.ecommerce.productservice.domain.repository.ProductRepository;
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

import java.security.InvalidParameterException;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void 상품_등록_성공() {
        // given
        Long sellerId = 1L;
        CreateProductRequest request = new CreateProductRequest("사과", "맛있는 사과", 1000L);
        Product savedProduct = Product.create(sellerId, request.name(), request.description(), request.price());
        ReflectionTestUtils.setField(savedProduct, "id", 1L);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        Long result = productService.createProduct(sellerId, "SELLER", request);

        // then
        assertThat(result).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void SELLER_아니면_상품_등록_실패() {
        // given
        CreateProductRequest request = new CreateProductRequest("사과", "맛있는 사과", 1000L);

        // when & then
        assertThatThrownBy(() -> productService.createProduct(1L, "MEMBER", request))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void 상품_목록_조회() {
        // given
        SearchRequest request = new SearchRequest(null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductListResponse> expected = new PageImpl<>(List.of(
                new ProductListResponse("사과", 1000L, ProductStatus.AVAILABLE)
        ));
        given(productRepository.getProductPage(request, pageable)).willReturn(expected);

        // when
        Page<ProductListResponse> result = productService.getProductPage(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
    }

    @Test
    void 내_상품_목록_조회_성공() {
        // given
        Long sellerId = 1L;
        SearchRequest request = new SearchRequest(null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductListResponse> expected = new PageImpl<>(List.of(
                new ProductListResponse("사과", 1000L, ProductStatus.AVAILABLE)
        ));
        given(productRepository.getMyProductPage(sellerId, request, pageable)).willReturn(expected);

        // when
        Page<ProductListResponse> result = productService.getMyProductPage(sellerId, "SELLER", request, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
    }

    @Test
    void SELLER_아니면_내_상품_목록_조회_실패() {
        // given
        SearchRequest request = new SearchRequest(null);
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> productService.getMyProductPage(1L, "MEMBER", request, pageable))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void 상품_수정_성공() {
        // given
        Long sellerId = 1L;
        Product product = Product.create(sellerId, "사과", "맛있는 사과", 1000L);
        ReflectionTestUtils.setField(product, "id", 1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        CreateProductRequest request = new CreateProductRequest("수정된 사과", "더 맛있는 사과", 2000L);

        // when
        Long result = productService.updateProduct(1L, sellerId, request);

        // then
        assertThat(result).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("수정된 사과");
        assertThat(product.getPrice()).isEqualTo(2000L);
    }

    @Test
    void 본인_상품_아니면_수정_실패() {
        // given
        Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(1L, 2L, new CreateProductRequest("수정", "수정", 2000L)))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void 상품_수정_상품_없으면_실패() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(1L, 1L, new CreateProductRequest("수정", "수정", 2000L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void 상품_삭제_성공() {
        // given
        Long sellerId = 1L;
        Product product = Product.create(sellerId, "사과", "맛있는 사과", 1000L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        productService.deleteProduct(1L, sellerId);

        // then
        verify(productRepository).delete(product);
    }

    @Test
    void 본인_상품_아니면_삭제_실패() {
        // given
        Product product = Product.create(1L, "사과", "맛있는 사과", 1000L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(1L, 2L))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void 상품_삭제_상품_없으면_실패() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
