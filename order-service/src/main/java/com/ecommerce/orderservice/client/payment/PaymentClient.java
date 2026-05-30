package com.ecommerce.orderservice.client.payment;

import com.ecommerce.orderservice.client.payment.dto.req.CreatePaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service", fallbackFactory = PaymentClientFallbackFactory.class)
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    ResponseEntity<Long> createPayment(@RequestHeader("X-Member-Id") Long memberId,
                                       @RequestBody CreatePaymentRequest request);

    @PatchMapping("/api/v1/payment/{orderId}/cancel")
    ResponseEntity<Void> cancelPayment(@PathVariable Long orderId);
}
