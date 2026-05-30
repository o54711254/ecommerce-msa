package com.ecommerce.paymentservice.domain.controller;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.dto.req.PaymentWebhookRequest;
import com.ecommerce.paymentservice.domain.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getPaymentList(@RequestHeader("X-Member-Id") Long memberId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/id")
    public ResponseEntity<?> getPaymentDetail() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Long> createPayment(@RequestHeader("X-Member-Id") Long memberId,
                                           @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(memberId, request));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPaymentByOrderId(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhookPayment(@RequestBody PaymentWebhookRequest request) {
        paymentService.webhook(request);
        return ResponseEntity.ok().build();
    }
}
