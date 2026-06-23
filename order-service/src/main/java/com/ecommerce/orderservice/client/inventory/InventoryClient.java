package com.ecommerce.orderservice.client.inventory;

import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", fallbackFactory = InventoryClientFallbackFactory.class)
public interface InventoryClient {

    @PutMapping("/api/v1/inventory/decrease")
    void decreaseInventory(@RequestBody DecreaseProductInventoryRequest request);
}
