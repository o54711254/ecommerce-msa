package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.dto.req.PaymentWebhookRequest;
import com.ecommerce.paymentservice.domain.dto.res.PaymentResponse;
import com.ecommerce.paymentservice.domain.entity.Payment;
import com.ecommerce.paymentservice.domain.entity.PaymentStatus;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import com.ecommerce.paymentservice.global.exception.custom.PaymentAccessDeniedException;
import com.ecommerce.paymentservice.global.exception.custom.PaymentCancelNotAllowedException;
import com.ecommerce.paymentservice.global.exception.custom.PaymentNotFoundException;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import com.ecommerce.paymentservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ProcessedEventService processedEventService;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentList(Long memberId) {
        return paymentRepository.findAllByMemberId(memberId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
        if (!payment.getMemberId().equals(memberId)) {
            throw new PaymentAccessDeniedException();
        }
        return PaymentResponse.from(payment);
    }

    @Transactional
    public Long createPayment(KafkaTopic kafkaTopic, Long memberId, CreatePaymentRequest request) {
        if (!processedEventService.saveOrSkipOrderEvent(kafkaTopic, request.orderId())) {
            return null;
        }
        Payment payment = Payment.create(memberId, request.orderId(), request.amount());
        return paymentRepository.save(payment).getId();
    }


    @Transactional
    public void cancelPaymentByOrderId(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        switch (payment.getPaymentStatus()) {
            case SUCCESS -> payment.updatePaymentStatus(PaymentStatus.REFUNDED);
            case PENDING -> payment.updatePaymentStatus(PaymentStatus.CANCELLED);
            default -> throw new PaymentCancelNotAllowedException();
        }
    }

    @Transactional
    public void webhook(PaymentWebhookRequest request) {
        String status = request.status();
        Payment payment = getPaymentByOrderId(request.orderId());
        switch (status) {
            case "SUCCESS" -> {
                payment.updatePaymentStatus(PaymentStatus.SUCCESS);
                paymentEventProducer.sendPaymentSuccess(
                        new PaymentSuccessEvent(payment.getOrderId(), payment.getMemberId(), payment.getId(), payment.getAmount())
                );
            }
            case "FAILED" -> {
                payment.updatePaymentStatus(PaymentStatus.FAILED);
                paymentEventProducer.sendPaymentFailed(new PaymentFailedEvent(payment.getOrderId(), payment.getMemberId()));
            }
        }
    }

    private Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(PaymentNotFoundException::new);
    }

}
