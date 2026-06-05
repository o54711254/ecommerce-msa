package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.AbstractIntegrationTest;
import com.ecommerce.paymentservice.domain.dto.req.PaymentWebhookRequest;
import com.ecommerce.paymentservice.domain.entity.Payment;
import com.ecommerce.paymentservice.domain.entity.PaymentStatus;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import com.ecommerce.paymentservice.global.exception.custom.PaymentCancelNotAllowedException;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private PaymentRepository paymentRepository;

    @MockitoBean private PaymentEventProducer paymentEventProducer;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Nested
    @DisplayName("cancelPaymentByOrderId - 결제 취소")
    class CancelPaymentByOrderIdTest {

        @Test
        void 성공_SUCCESS_REFUNDED로_변경() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            payment.updatePaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            paymentService.cancelPaymentByOrderId(10L);

            Payment updated = paymentRepository.findByOrderId(10L).orElseThrow();
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        void 성공_PENDING_CANCELLED로_변경() {
            paymentRepository.save(Payment.create(1L, 10L, 50000L));

            paymentService.cancelPaymentByOrderId(10L);

            Payment updated = paymentRepository.findByOrderId(10L).orElseThrow();
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        void 실패_취소_불가_상태() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            payment.updatePaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            assertThatThrownBy(() -> paymentService.cancelPaymentByOrderId(10L))
                    .isInstanceOf(PaymentCancelNotAllowedException.class);
        }
    }

    @Nested
    @DisplayName("webhook - 결제 웹훅 처리")
    class WebhookTest {

        @Test
        void 성공_SUCCESS_상태_DB_반영() {
            paymentRepository.save(Payment.create(1L, 10L, 50000L));

            paymentService.webhook(new PaymentWebhookRequest(10L, "SUCCESS"));

            Payment updated = paymentRepository.findByOrderId(10L).orElseThrow();
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        void 성공_FAILED_상태_DB_반영() {
            paymentRepository.save(Payment.create(1L, 10L, 50000L));

            paymentService.webhook(new PaymentWebhookRequest(10L, "FAILED"));

            Payment updated = paymentRepository.findByOrderId(10L).orElseThrow();
            assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}
