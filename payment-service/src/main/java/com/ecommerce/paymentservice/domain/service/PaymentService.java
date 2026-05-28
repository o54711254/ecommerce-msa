package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.dto.req.PaymentWebhookRequest;
import com.ecommerce.paymentservice.domain.entity.Payment;
import com.ecommerce.paymentservice.domain.entity.PaymentStatus;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import com.ecommerce.paymentservice.global.exception.custom.PaymentNotFoundException;
import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public Long createPayment(Long memberId, CreatePaymentRequest request) {
        Payment payment = Payment.create(memberId, request.orderId(), request.amount());
        return paymentRepository.save(payment).getId();
    }

    @Transactional
    public void webhook(PaymentWebhookRequest request) {
        String status = request.status();
        Payment payment = getPayment(request);
        switch (status) {
            case "SUCCESS" -> {
                payment.updatePaymentStatus(PaymentStatus.SUCCESS);
                paymentEventProducer.sendPaymentSuccess(
                        new PaymentSuccessEvent(payment.getOrderId(), payment.getMemberId(), payment.getAmount())
                );
            }
            case "FAILED" -> {
                payment.updatePaymentStatus(PaymentStatus.FAILED);
            }
        }
    }

    private Payment getPayment(PaymentWebhookRequest request) {
        return paymentRepository.findByOrderId(request.orderId()).orElseThrow(PaymentNotFoundException::new);
    }
}
