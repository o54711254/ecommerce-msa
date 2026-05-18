package com.ecommerce.productservice.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory")
    ResponseEntity<Long> createInventory(@RequestBody CreateInventoryRequest request);

    @DeleteMapping("/api/v1/inventory/{id}")
    ResponseEntity<Void> deleteInventory(@PathVariable Long id);
}
