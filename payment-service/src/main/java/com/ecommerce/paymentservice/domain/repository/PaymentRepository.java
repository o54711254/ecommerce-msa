package com.ecommerce.paymentservice.domain.repository;

import com.ecommerce.paymentservice.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
