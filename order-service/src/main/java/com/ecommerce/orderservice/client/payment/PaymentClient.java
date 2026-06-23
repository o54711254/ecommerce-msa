package com.ecommerce.orderservice.client.payment;

import com.ecommerce.orderservice.client.payment.dto.req.CreatePaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service", fallbackFactory = PaymentClientFallbackFactory.class)
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    Long createPayment(@RequestHeader("X-Member-Id") Long memberId,
                       @RequestBody CreatePaymentRequest request);

    @PostMapping("/api/v1/payment/{orderId}/cancel")
    void cancelPayment(@PathVariable Long orderId);
}
