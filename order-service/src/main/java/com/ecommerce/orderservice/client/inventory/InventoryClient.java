package com.ecommerce.orderservice.client.inventory;

import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", fallbackFactory = InventoryClientFallbackFactory.class)
public interface InventoryClient {

    @PutMapping("/api/v1/inventory/decrease")
    ResponseEntity<Void> decreaseInventory(@RequestBody DecreaseProductInventoryRequest request);
}
