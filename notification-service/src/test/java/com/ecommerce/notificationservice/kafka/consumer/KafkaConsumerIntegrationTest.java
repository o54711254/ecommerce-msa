package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.AbstractIntegrationTest;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.repository.NotificationRepository;
import com.ecommerce.notificationservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=true")
@Import(KafkaConsumerIntegrationTest.EventCaptureConfig.class)
class KafkaConsumerIntegrationTest extends AbstractIntegrationTest {

    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"));

    static {
        KAFKA.start();
    }

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private EventCapture eventCapture;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        notificationRepository.deleteAll();
        eventCapture.clear();
    }

    @TestConfiguration
    static class EventCaptureConfig {
        @Bean
        EventCapture eventCapture() {
            return new EventCapture();
        }
    }

    static class EventCapture {
        final List<String> paymentSuccessDlt = new CopyOnWriteArrayList<>();
        final List<String> paymentFailedDlt = new CopyOnWriteArrayList<>();
        final List<String> orderCancelledDlt = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = "payment.success-dlt", groupId = "test-notif-pay-success-dlt",
                properties = "auto.offset.reset=earliest")
        void onPaymentSuccessDlt(String msg) {
            paymentSuccessDlt.add(msg);
        }

        @KafkaListener(topics = "payment.failed-dlt", groupId = "test-notif-pay-failed-dlt",
                properties = "auto.offset.reset=earliest")
        void onPaymentFailedDlt(String msg) {
            paymentFailedDlt.add(msg);
        }

        @KafkaListener(topics = "order.cancelled-dlt", groupId = "test-notif-order-cancelled-dlt",
                properties = "auto.offset.reset=earliest")
        void onOrderCancelledDlt(String msg) {
            orderCancelledDlt.add(msg);
        }

        void clear() {
            paymentSuccessDlt.clear();
            paymentFailedDlt.clear();
            orderCancelledDlt.clear();
        }
    }

    @Nested
    @DisplayName("handlePaymentSuccess - 결제 완료 알림 처리")
    class HandlePaymentSuccessTest {

        @Test
        void 성공_PAYMENT_SUCCESS_알림_생성() {
            kafkaTemplate.send("payment.success", "1",
                    "{\"orderId\":1,\"memberId\":1,\"paymentId\":5,\"amount\":5000}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(notificationRepository.findAll().get(0).getType())
                                .isEqualTo(NotificationType.PAYMENT_SUCCESS);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.PAYMENT_SUCCESS, 1L)).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_저장() {
            String payload = "{\"orderId\":2,\"memberId\":1,\"paymentId\":5,\"amount\":5000}";

            kafkaTemplate.send("payment.success", "2", payload);
            kafkaTemplate.send("payment.success", "2", payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(processedEventRepository.count()).isEqualTo(1);
                    });
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("payment.success", "1", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.paymentSuccessDlt).hasSize(1));
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed - 결제 실패 알림 처리")
    class HandlePaymentFailedTest {

        @Test
        void 성공_PAYMENT_FAILED_알림_생성() {
            kafkaTemplate.send("payment.failed", "3",
                    "{\"orderId\":3,\"memberId\":1}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(notificationRepository.findAll().get(0).getType())
                                .isEqualTo(NotificationType.PAYMENT_FAILED);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.PAYMENT_FAILED, 3L)).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_저장() {
            String payload = "{\"orderId\":4,\"memberId\":1}";

            kafkaTemplate.send("payment.failed", "4", payload);
            kafkaTemplate.send("payment.failed", "4", payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(processedEventRepository.count()).isEqualTo(1);
                    });
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("payment.failed", "3", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.paymentFailedDlt).hasSize(1));
        }
    }

    @Nested
    @DisplayName("handleOrderCancelled - 주문 취소 알림 처리")
    class HandleOrderCancelledTest {

        @Test
        void 성공_ORDER_CANCELED_알림_생성() {
            kafkaTemplate.send("order.cancelled", "1",
                    "{\"memberId\":1,\"orderId\":1,\"itemInfoList\":[]}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(notificationRepository.findAll().get(0).getType())
                                .isEqualTo(NotificationType.ORDER_CANCELED);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.ORDER_CANCELLED, 1L)).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_저장() {
            String payload = "{\"memberId\":1,\"orderId\":2,\"itemInfoList\":[]}";

            kafkaTemplate.send("order.cancelled", "2", payload);
            kafkaTemplate.send("order.cancelled", "2", payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(notificationRepository.count()).isEqualTo(1);
                        assertThat(processedEventRepository.count()).isEqualTo(1);
                    });
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("order.cancelled", "1", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.orderCancelledDlt).hasSize(1));
        }
    }
}
