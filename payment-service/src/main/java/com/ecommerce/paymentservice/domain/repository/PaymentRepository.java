package com.ecommerce.paymentservice.domain.repository;

import com.ecommerce.paymentservice.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByMemberId(Long memberId);
}
