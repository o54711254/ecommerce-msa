package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
