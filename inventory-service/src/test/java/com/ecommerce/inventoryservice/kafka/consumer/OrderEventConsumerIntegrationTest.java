package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.AbstractIntegrationTest;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.ecommerce.inventoryservice.domain.entity.InventoryEventType;
import com.ecommerce.inventoryservice.domain.repository.InventoryRepository;
import com.ecommerce.inventoryservice.domain.repository.ProcessedEventRepository;
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

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProcessedEventRepository processedEventRepository;
    @Autowired
    private EventCapture eventCapture;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        processedEventRepository.deleteAll();
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
        final List<String> decreased = new CopyOnWriteArrayList<>();
        final List<String> failed = new CopyOnWriteArrayList<>();
        final List<String> dlt = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = "inventory.decreased", groupId = "test-decreased")
        void onDecreased(String msg) {
            decreased.add(msg);
        }

        @KafkaListener(topics = "inventory.failed", groupId = "test-failed")
        void onFailed(String msg) {
            failed.add(msg);
        }

        // DLT 토픽은 DeadLetterPublishingRecoverer가 생성하므로 consumer 구독 시점에 존재하지 않음
        // earliest로 설정해야 파티션 할당 후 offset 0부터 읽어 메시지를 놓치지 않음
        @KafkaListener(topics = "order.created-dlt", groupId = "test-dlt",
                properties = "auto.offset.reset=earliest")
        void onDlt(String msg) {
            dlt.add(msg);
        }

        void clear() {
            decreased.clear();
            failed.clear();
            dlt.clear();
        }
    }

    @Nested
    @DisplayName("handleOrderCreated - 주문 생성 이벤트 처리")
    class HandleOrderCreatedTest {

        @Test
        void 성공_재고차감_및_inventory_decreased_발행() {
            inventoryRepository.save(Inventory.create(1L, 10));
            String payload = """
                    {"memberId":1,"orderId":1,"amount":1000,"itemInfoList":[{"productId":1,"quantity":3}]}""";

            kafkaTemplate.send("order.created", "1", payload);

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(7);
                        assertThat(eventCapture.decreased).hasSize(1);
                        assertThat(eventCapture.decreased.get(0)).contains("\"orderId\":1");
                    });
        }

        @Test
        void 실패_재고_부족시_inventory_failed_발행() {
            inventoryRepository.save(Inventory.create(1L, 2));
            String payload = """
                    {"memberId":1,"orderId":2,"amount":1000,"itemInfoList":[{"productId":1,"quantity":10}]}""";

            kafkaTemplate.send("order.created", "2", payload);

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(eventCapture.failed).hasSize(1);
                        assertThat(eventCapture.failed.get(0)).contains("\"orderId\":2");
                        assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(2);
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_차감() {
            inventoryRepository.save(Inventory.create(1L, 10));
            String payload = """
                    {"memberId":1,"orderId":3,"amount":1000,"itemInfoList":[{"productId":1,"quantity":3}]}""";

            kafkaTemplate.send("order.created", "3", payload);
            kafkaTemplate.send("order.created", "3", payload);

            // during(2s): 2초 동안 조건이 유지돼야 통과 → 두 번째 메시지도 처리됐음을 보장
            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {

                        assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(7);
                        assertThat(processedEventRepository.existsByEventTypeAndOrderId(InventoryEventType.DECREASE, 3L)).isTrue();
                        long decreasedForThisOrder = eventCapture.decreased.stream()
                                .filter(m -> m.contains("\"orderId\":3"))
                                .count();
                        assertThat(decreasedForThisOrder).isEqualTo(1);
                    });

        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("order.created", "4", "invalid-json{{{");

            // DefaultErrorHandler: 3회 재시도(1000ms 간격) 후 DLT 이동 → 최소 3초 소요
            Awaitility.await().atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.dlt).hasSize(1));
        }
    }
}
