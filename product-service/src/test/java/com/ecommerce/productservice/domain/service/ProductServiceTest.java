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
        given(productRepository.getProductList(request, pageable)).willReturn(expected);

        // when
        Page<ProductListResponse> result = productService.getProductList(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).productName()).isEqualTo("사과");
    }
}
