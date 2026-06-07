package com.ecommerce.inventoryservice.domain.repository;

import com.ecommerce.inventoryservice.domain.entity.InventoryEventType;
import com.ecommerce.inventoryservice.domain.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventTypeAndOrderId(InventoryEventType eventType, Long orderId);
}
