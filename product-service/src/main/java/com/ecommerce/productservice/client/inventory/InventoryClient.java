package com.ecommerce.productservice.client.inventory;

import com.ecommerce.productservice.client.inventory.dto.req.CreateInventoryRequest;
import com.ecommerce.productservice.client.inventory.dto.res.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", fallbackFactory = InventoryClientFallbackFactory.class)
public interface InventoryClient {

    @PostMapping("/api/v1/inventory")
    Long createInventory(@RequestBody CreateInventoryRequest request);

    @GetMapping("/api/v1/inventory/{productId}")
    InventoryResponse getInventory(@PathVariable Long productId);

    @DeleteMapping("/api/v1/inventory/{productId}")
    void deleteInventory(@PathVariable Long productId);
}
