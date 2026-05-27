package com.ecommerce.orderservice.client.payment;

import com.ecommerce.orderservice.client.payment.dto.req.CreatePaymentRequest;
import com.ecommerce.orderservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentClientFallbackFactory implements FallbackFactory<PaymentClient> {

    @Override
    public PaymentClient create(Throwable cause) {
        log.error("payment-service 호출 실패 - {}", cause.getMessage());
        return new PaymentClient() {

            @Override
            public ResponseEntity<Long> createPayment(Long memberId, CreatePaymentRequest request) {
                throw new ExternalServiceException("payment-service", cause);
            }
        };
    }
}
