package com.ecommerce.paymentservice.kafka.consumer;

import com.ecommerce.paymentservice.AbstractIntegrationTest;
import com.ecommerce.paymentservice.domain.entity.PaymentStatus;
import com.ecommerce.paymentservice.domain.repository.PaymentRepository;
import com.ecommerce.paymentservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
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
@Import(OrderEventConsumerIntegrationTest.EventCaptureConfig.class)
class OrderEventConsumerIntegrationTest extends AbstractIntegrationTest {

    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"));

    static {
        KAFKA.start();
    }

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private EventCapture eventCapture;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        paymentRepository.deleteAll();
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
        final List<String> inventoryDecreasedDlt = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = "inventory.decreased-dlt", groupId = "test-inv-decreased-dlt",
                properties = "auto.offset.reset=earliest")
        void onDlt(String msg) {
            inventoryDecreasedDlt.add(msg);
        }

        void clear() {
            inventoryDecreasedDlt.clear();
        }
    }

    @Nested
    @DisplayName("handleInventoryDecreased - 재고 차감 완료 처리")
    class HandleInventoryDecreasedTest {

        @Test
        void 성공_Payment_PENDING_생성() {
            kafkaTemplate.send("inventory.decreased", "1",
                    "{\"orderId\":1,\"memberId\":1,\"amount\":5000}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(paymentRepository.findByOrderId(1L)).isPresent();
                        assertThat(paymentRepository.findByOrderId(1L).orElseThrow().getPaymentStatus())
                                .isEqualTo(PaymentStatus.PENDING);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.INVENTORY_DECREASED, 1L)).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_처리() {
            String payload = "{\"orderId\":2,\"memberId\":1,\"amount\":5000}";

            kafkaTemplate.send("inventory.decreased", "2", payload);
            kafkaTemplate.send("inventory.decreased", "2", payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(paymentRepository.count()).isEqualTo(1);
                        assertThat(processedEventRepository.count()).isEqualTo(1);
                    });
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("inventory.decreased", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.inventoryDecreasedDlt).hasSize(1));
        }
    }
}
