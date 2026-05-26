package com.ecommerce.orderservice.client.product;

import com.ecommerce.orderservice.client.product.dto.ProductNameResponse;
import com.ecommerce.orderservice.client.product.dto.ProductPriceResponse;
import com.ecommerce.orderservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error("product-service 호출 실패 - {}", cause.getMessage());
        return new ProductClient() {
            @Override
            public ResponseEntity<ProductPriceResponse> getPriceMap(List<Long> productIds) {
                throw new ExternalServiceException("product-service", cause);
            }

            @Override
            public ResponseEntity<ProductNameResponse> getNamesMap(List<Long> productIds) {
                log.warn("product-service 불가 - 상품명 없이 응답");
                return ResponseEntity.ok(new ProductNameResponse(Map.of()));
            }
        };
    }
}
