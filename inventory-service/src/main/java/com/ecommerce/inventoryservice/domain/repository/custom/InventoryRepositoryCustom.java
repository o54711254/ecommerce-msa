package com.ecommerce.inventoryservice.domain.repository.custom;

import com.ecommerce.inventoryservice.domain.entity.Inventory;

import java.util.List;

public interface InventoryRepositoryCustom {

    List<Inventory> findInventoriesByProductIdForLock(List<Long> productIds);
}
