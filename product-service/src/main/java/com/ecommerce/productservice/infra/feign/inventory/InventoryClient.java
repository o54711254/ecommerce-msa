package com.ecommerce.productservice.infra.feign.inventory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory")
    ResponseEntity<Long> createInventory(@RequestBody CreateInventoryRequest request);

    @GetMapping("/api/v1/inventory/{productId}")
    ResponseEntity<InventoryResponse> getInventory(@PathVariable Long productId);

    @DeleteMapping("/api/v1/inventory/{productId}")
    ResponseEntity<Void> deleteInventory(@PathVariable Long productId);
}
