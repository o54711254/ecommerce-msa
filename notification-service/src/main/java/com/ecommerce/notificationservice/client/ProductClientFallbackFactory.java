package com.ecommerce.notificationservice.client;

import com.ecommerce.notificationservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error("product-service 호출 실패 - {}", cause.getMessage());
        return new ProductClient() {

            @Override
            public Long getSellerId(Long productId) {
                throw new ExternalServiceException("product-service", cause);
            }
        };
    }
}
