package com.ecommerce.productservice.client.inventory;

import com.ecommerce.productservice.client.inventory.dto.req.CreateInventoryRequest;
import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import com.ecommerce.productservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        log.error("inventory-service 호출 실패 - {}", cause.getMessage());
        return new InventoryClient() {
            @Override
            public Long createInventory(CreateInventoryRequest request) {
                throw new ExternalServiceException("inventory-service", cause);
            }

            @Override
            public InventoryResponse getInventory(Long productId) {
                throw new ExternalServiceException("inventory-service", cause);
            }

            @Override
            public void deleteInventory(Long productId) {
                throw new ExternalServiceException("inventory-service", cause);
            }
        };
    }
}
