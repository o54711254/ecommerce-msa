package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.AbstractIntegrationTest;
import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.dto.req.PaymentWebhookRequest;
import com.ecommerce.paymentservice.domain.dto.res.PaymentResponse;
import com.ecommerce.paymentservice.domain.entity.Payment;
import com.ecommerce.paymentservice.domain.entity.PaymentStatus;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import com.ecommerce.paymentservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.paymentservice.global.exception.custom.PaymentAccessDeniedException;
import com.ecommerce.paymentservice.global.exception.custom.PaymentCancelNotAllowedException;
import com.ecommerce.paymentservice.global.exception.custom.PaymentNotFoundException;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @MockitoBean private PaymentEventProducer paymentEventProducer;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Nested
    @DisplayName("getPaymentList - 결제 목록 조회")
    class GetPaymentListTest {

        @Test
        void 성공() {
            paymentRepository.save(Payment.create(1L, 10L, 30000L));
            paymentRepository.save(Payment.create(1L, 11L, 50000L));
            paymentRepository.save(Payment.create(2L, 12L, 20000L));

            List<PaymentResponse> result = paymentService.getPaymentList(1L);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.paymentId() != null);
        }

        @Test
        void 성공_결제내역_없음() {
            List<PaymentResponse> result = paymentService.getPaymentList(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPaymentDetail - 결제 상세 조회")
    class GetPaymentDetailTest {

        @Test
        void 성공() {
            Payment saved = paymentRepository.save(Payment.create(1L, 10L, 50000L));

            PaymentResponse result = paymentService.getPaymentDetail(1L, saved.getId());

            assertThat(result.paymentId()).isEqualTo(saved.getId());
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.amount()).isEqualTo(50000L);
            assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        void 실패_결제_없음() {
            assertThatThrownBy(() -> paymentService.getPaymentDetail(1L, 999L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        void 실패_본인_결제_아님() {
            Payment saved = paymentRepository.save(Payment.create(1L, 10L, 50000L));

            assertThatThrownBy(() -> paymentService.getPaymentDetail(2L, saved.getId()))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("createPayment - 결제 생성")
    class CreatePaymentTest {

        @Test
        void 성공_결제_DB_저장() {
            Long paymentId = paymentService.createPayment(KafkaTopic.INVENTORY_DECREASED, 1L,
                    new CreatePaymentRequest(10L, 50000L));

            assertThat(paymentId).isNotNull();
            assertThat(paymentRepository.findById(paymentId)).isPresent();
            assertThat(processedEventRepository.count()).isEqualTo(1);
        }

        @Test
        void 멱등성_같은_orderId_두번_호출시_한_번만_저장() {
            paymentService.createPayment(KafkaTopic.INVENTORY_DECREASED, 1L,
                    new CreatePaymentRequest(10L, 50000L));
            paymentService.createPayment(KafkaTopic.INVENTORY_DECREASED, 1L,
                    new CreatePaymentRequest(10L, 50000L));

            assertThat(paymentRepository.count()).isEqualTo(1);
            assertThat(processedEventRepository.count()).isEqualTo(1);
        }
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
