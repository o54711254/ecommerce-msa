package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.repository.custom.OrderRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
}
