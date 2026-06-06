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
import com.ecommerce.paymentservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
import com.ecommerce.paymentservice.kafka.producer.PaymentEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentEventProducer paymentEventProducer;
    @InjectMocks private PaymentService paymentService;

    @Nested
    @DisplayName("getPaymentList - 결제 목록 조회")
    class GetPaymentListTest {

        @Test
        void 성공() {
            Payment p1 = Payment.create(1L, 10L, 30000L);
            Payment p2 = Payment.create(1L, 11L, 50000L);
            given(paymentRepository.findAllByMemberId(1L)).willReturn(List.of(p1, p2));

            List<PaymentResponse> result = paymentService.getPaymentList(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).orderId()).isEqualTo(10L);
            assertThat(result.get(1).orderId()).isEqualTo(11L);
        }

        @Test
        void 성공_결제내역_없음() {
            given(paymentRepository.findAllByMemberId(1L)).willReturn(List.of());

            List<PaymentResponse> result = paymentService.getPaymentList(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPaymentDetail - 결제 상세 조회")
    class GetPaymentDetailTest {

        @Test
        void 성공() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            ReflectionTestUtils.setField(payment, "id", 1L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            PaymentResponse result = paymentService.getPaymentDetail(1L, 1L);

            assertThat(result.paymentId()).isEqualTo(1L);
            assertThat(result.orderId()).isEqualTo(10L);
            assertThat(result.amount()).isEqualTo(50000L);
        }

        @Test
        void 실패_결제_없음() {
            given(paymentRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentDetail(1L, 999L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        void 실패_본인_결제_아님() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            ReflectionTestUtils.setField(payment, "id", 1L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.getPaymentDetail(2L, 1L))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("createPayment - 결제 생성")
    class CreatePaymentTest {

        @Test
        void 성공() {
            Long memberId = 1L;
            CreatePaymentRequest request = new CreatePaymentRequest(10L, 50000L);
            Payment payment = Payment.create(memberId, 10L, 50000L);
            ReflectionTestUtils.setField(payment, "id", 1L);
            given(paymentRepository.save(any(Payment.class))).willReturn(payment);

            Long paymentId = paymentService.createPayment(memberId, request);

            assertThat(paymentId).isEqualTo(1L);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("cancelPaymentByOrderId - 결제 취소")
    class CancelPaymentByOrderIdTest {

        @Test
        void 성공_SUCCESS_REFUNDED로_변경() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            payment.updatePaymentStatus(PaymentStatus.SUCCESS);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            paymentService.cancelPaymentByOrderId(10L);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        void 성공_PENDING_CANCELLED로_변경() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            paymentService.cancelPaymentByOrderId(10L);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        void 실패_결제_없음() {
            given(paymentRepository.findByOrderId(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.cancelPaymentByOrderId(999L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        void 실패_취소_불가_상태() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            payment.updatePaymentStatus(PaymentStatus.FAILED);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPaymentByOrderId(10L))
                    .isInstanceOf(PaymentCancelNotAllowedException.class);
        }
    }

    @Nested
    @DisplayName("webhook - 결제 웹훅 처리")
    class WebhookTest {

        @Test
        void 성공_SUCCESS_상태변경_및_이벤트_발행() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            paymentService.webhook(new PaymentWebhookRequest(10L, "SUCCESS"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            ArgumentCaptor<PaymentSuccessEvent> captor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
            verify(paymentEventProducer).sendPaymentSuccess(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(10L);
            assertThat(captor.getValue().memberId()).isEqualTo(1L);
            assertThat(captor.getValue().amount()).isEqualTo(50000L);
        }

        @Test
        void 성공_FAILED_상태변경_및_이벤트_발행() {
            Payment payment = Payment.create(1L, 10L, 50000L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            paymentService.webhook(new PaymentWebhookRequest(10L, "FAILED"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
            ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            verify(paymentEventProducer).sendPaymentFailed(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(10L);
            assertThat(captor.getValue().memberId()).isEqualTo(1L);
        }

        @Test
        void 실패_결제_없음() {
            given(paymentRepository.findByOrderId(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.webhook(new PaymentWebhookRequest(999L, "SUCCESS")))
                    .isInstanceOf(PaymentNotFoundException.class);
            verifyNoInteractions(paymentEventProducer);
        }
    }
}
