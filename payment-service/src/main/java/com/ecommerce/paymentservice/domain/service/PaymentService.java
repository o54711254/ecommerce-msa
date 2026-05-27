package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.entity.Payment;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Long createPayment(Long memberId, CreatePaymentRequest request) {
        Payment payment = Payment.create(memberId, request.orderId(), request.amount());
        return paymentRepository.save(payment).getId();
    }
}
