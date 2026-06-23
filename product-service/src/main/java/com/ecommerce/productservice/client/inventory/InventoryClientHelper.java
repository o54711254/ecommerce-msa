package com.ecommerce.productservice.client.inventory;

import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClientHelper {

    private final InventoryClient inventoryClient;

    @Retry(name = "inventory-service", fallbackMethod = "getInventoryFallback")
    public InventoryResponse getInventory(Long productId) {
        return inventoryClient.getInventory(productId);
    }

    private InventoryResponse getInventoryFallback(Long productId, Exception e) {
        log.warn("inventory-service 재시도 모두 실패 - 재고 정보 없이 응답");
        return new InventoryResponse(productId, null);
    }
}