package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.AbstractIntegrationTest;
import com.ecommerce.orderservice.client.payment.PaymentClient;
import com.ecommerce.orderservice.client.product.ProductClient;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.repository.OrderItemRepository;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private EventCapture eventCapture;

    @MockitoBean private ProductClient productClient;
    @MockitoBean private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        eventCapture.clear();
    }

    Order saveOrder() {
        return orderRepository.save(Order.create(1L, List.of(OrderItem.create(1L, 2, 1000L))));
    }

    @TestConfiguration
    static class EventCaptureConfig {
        @Bean
        EventCapture eventCapture() {
            return new EventCapture();
        }
    }

    static class EventCapture {
        final List<String> orderFailed = new CopyOnWriteArrayList<>();
        final List<String> inventoryFailedDlt = new CopyOnWriteArrayList<>();
        final List<String> paymentSuccessDlt = new CopyOnWriteArrayList<>();
        final List<String> paymentFailedDlt = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = "order.failed", groupId = "test-order-failed",
                properties = "auto.offset.reset=earliest")
        void onOrderFailed(String msg) {
            orderFailed.add(msg);
        }

        @KafkaListener(topics = "inventory.failed-dlt", groupId = "test-inv-failed-dlt",
                properties = "auto.offset.reset=earliest")
        void onInventoryFailedDlt(String msg) {
            inventoryFailedDlt.add(msg);
        }

        @KafkaListener(topics = "payment.success-dlt", groupId = "test-pay-success-dlt",
                properties = "auto.offset.reset=earliest")
        void onPaymentSuccessDlt(String msg) {
            paymentSuccessDlt.add(msg);
        }

        @KafkaListener(topics = "payment.failed-dlt", groupId = "test-pay-failed-dlt",
                properties = "auto.offset.reset=earliest")
        void onPaymentFailedDlt(String msg) {
            paymentFailedDlt.add(msg);
        }

        void clear() {
            orderFailed.clear();
            inventoryFailedDlt.clear();
            paymentSuccessDlt.clear();
            paymentFailedDlt.clear();
        }
    }

    @Nested
    @DisplayName("handleInventoryFailed - 재고 차감 실패 처리")
    class HandleInventoryFailedTest {

        @Test
        void 성공_Order_FAILED로_변경() {
            Order order = saveOrder();
            kafkaTemplate.send("inventory.failed", String.valueOf(order.getId()),
                    "{\"orderId\":" + order.getId() + "}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(orderRepository.findById(order.getId()).orElseThrow().getOrderStatus())
                                .isEqualTo(OrderStatus.FAILED);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.INVENTORY_FAILED, order.getId())).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_처리() {
            Order order = saveOrder();
            String payload = "{\"orderId\":" + order.getId() + "}";

            kafkaTemplate.send("inventory.failed", String.valueOf(order.getId()), payload);
            kafkaTemplate.send("inventory.failed", String.valueOf(order.getId()), payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(processedEventRepository.count()).isEqualTo(1));
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("inventory.failed", "1", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.inventoryFailedDlt).hasSize(1));
        }
    }

    @Nested
    @DisplayName("handlePaymentSuccess - 결제 완료 처리")
    class HandlePaymentSuccessTest {

        @Test
        void 성공_Order_PAID로_변경() {
            Order order = saveOrder();
            kafkaTemplate.send("payment.success", String.valueOf(order.getId()),
                    "{\"orderId\":" + order.getId() + ",\"memberId\":1,\"paymentId\":5,\"amount\":2000}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(orderRepository.findById(order.getId()).orElseThrow().getOrderStatus())
                                .isEqualTo(OrderStatus.PAID);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.PAYMENT_SUCCESS, order.getId())).isTrue();
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_처리() {
            Order order = saveOrder();
            String payload = "{\"orderId\":" + order.getId() + ",\"memberId\":1,\"paymentId\":5,\"amount\":2000}";

            kafkaTemplate.send("payment.success", String.valueOf(order.getId()), payload);
            kafkaTemplate.send("payment.success", String.valueOf(order.getId()), payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(processedEventRepository.count()).isEqualTo(1));
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("payment.success", "2", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.paymentSuccessDlt).hasSize(1));
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed - 결제 실패 처리")
    class HandlePaymentFailedTest {

        @Test
        void 성공_Order_FAILED로_변경_및_order_failed_발행() {
            Order order = saveOrder();
            kafkaTemplate.send("payment.failed", String.valueOf(order.getId()),
                    "{\"orderId\":" + order.getId() + ",\"memberId\":1}");

            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(orderRepository.findById(order.getId()).orElseThrow().getOrderStatus())
                                .isEqualTo(OrderStatus.FAILED);
                        assertThat(processedEventRepository.existsByKafkaTopicAndOrderId(KafkaTopic.PAYMENT_FAILED, order.getId())).isTrue();
                        assertThat(eventCapture.orderFailed).hasSize(1);
                        assertThat(eventCapture.orderFailed.get(0)).contains("\"orderId\":" + order.getId());
                    });
        }

        @Test
        void 멱등성_같은_orderId_두번_발행시_한_번만_처리() {
            Order order = saveOrder();
            String payload = "{\"orderId\":" + order.getId() + ",\"memberId\":1}";

            kafkaTemplate.send("payment.failed", String.valueOf(order.getId()), payload);
            kafkaTemplate.send("payment.failed", String.valueOf(order.getId()), payload);

            Awaitility.await().during(2, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(processedEventRepository.count()).isEqualTo(1));
        }

        @Test
        void DLT_라우팅_잘못된_JSON() {
            kafkaTemplate.send("payment.failed", "3", "bad-json{{{");

            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(eventCapture.paymentFailedDlt).hasSize(1));
        }
    }
}
