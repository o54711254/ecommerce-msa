package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
