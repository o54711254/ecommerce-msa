package com.ecommerce.orderservice.client.inventory;

import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
import com.ecommerce.orderservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        log.error("inventory-service 호출 실패 - {}", cause.getMessage());
        return new InventoryClient() {
            @Override
            public ResponseEntity<Void> decreaseInventory(DecreaseProductInventoryRequest request) {
                throw new ExternalServiceException("inventory-service", cause);
            }
        };
    }
}
