package com.ecommerce.productservice.infra.feign.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public ResponseEntity<Long> createInventory(CreateInventoryRequest request) {
        throw new RuntimeException("Inventory NonAvailable");
    }

    @Override
    public ResponseEntity<InventoryResponse> getInventory(Long productId) {
        InventoryResponse inventoryResponse = new InventoryResponse(null, null);
        return ResponseEntity.ok(inventoryResponse);
    }

    @Override
    public ResponseEntity<Void> deleteInventory(Long productId) {
        throw new RuntimeException("Inventory NonAvailable");
    }
}
