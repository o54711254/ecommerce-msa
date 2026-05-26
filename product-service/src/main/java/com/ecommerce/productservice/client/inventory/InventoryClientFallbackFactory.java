package com.ecommerce.productservice.client.inventory;

import com.ecommerce.productservice.client.inventory.dto.req.CreateInventoryRequest;
import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import com.ecommerce.productservice.global.exception.ExternalServiceException;
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
            public ResponseEntity<Long> createInventory(CreateInventoryRequest request) {
                throw new ExternalServiceException("inventory-service", cause);
            }

            @Override
            public ResponseEntity<InventoryResponse> getInventory(Long productId) {
                log.warn("inventory-service 불가 - 재고 정보 없이 응답");
                return ResponseEntity.ok(new InventoryResponse(productId, null));
            }

            @Override
            public ResponseEntity<Void> deleteInventory(Long productId) {
                throw new ExternalServiceException("inventory-service", cause);
            }
        };
    }
}
