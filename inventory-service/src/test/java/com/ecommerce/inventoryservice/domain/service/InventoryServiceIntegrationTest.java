package com.ecommerce.inventoryservice.domain.service;

import com.ecommerce.inventoryservice.AbstractIntegrationTest;
import com.ecommerce.inventoryservice.domain.dto.req.DecreaseProductInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.req.OrderInfoRequest;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.ecommerce.inventoryservice.domain.repository.InventoryRepository;
import com.ecommerce.inventoryservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.inventoryservice.global.exception.custom.InsufficientStockException;
import com.ecommerce.inventoryservice.kafka.config.KafkaTopic;
import com.ecommerce.inventoryservice.kafka.dto.OrderItemInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Nested
    @DisplayName("decreaseProductInventory - 재고 차감")
    class DecreaseProductInventoryTest {

        @Test
        void 재고_부족시_InsufficientStockException() {
            inventoryRepository.save(Inventory.create(1L, 5));

            DecreaseProductInventoryRequest request = new DecreaseProductInventoryRequest(
                    List.of(new OrderInfoRequest(1L, 10))
            );

            assertThatThrownBy(() -> inventoryService.decreaseProductInventory(request))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        void 동시_요청시_oversell_방지() throws InterruptedException {
            // 재고 5개, 3개씩 2번 동시 차감 → 총 6개 요청이므로 한 번만 성공해야 함
            inventoryRepository.save(Inventory.create(1L, 5));

            int threadCount = 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();

            DecreaseProductInventoryRequest request = new DecreaseProductInventoryRequest(
                    List.of(new OrderInfoRequest(1L, 3))
            );

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        // 각 스레드는 독립 트랜잭션으로 실행됨
                        // PESSIMISTIC_WRITE 락 덕분에 먼저 락을 잡은 쪽이 차감하고,
                        // 나머지는 재고 부족으로 InsufficientStockException 발생
                        inventoryService.decreaseProductInventory(request);
                        successCount.incrementAndGet();
                    } catch (InsufficientStockException e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(1);
            assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("decreaseProductInventoryIdempotent - 멱등 재고 차감")
    class DecreaseProductInventoryIdempotentTest {

        @Test
        void 같은_orderId로_두_번_호출시_한_번만_차감() {
            inventoryRepository.save(Inventory.create(1L, 10));
            Long orderId = 100L;
            DecreaseProductInventoryRequest request = new DecreaseProductInventoryRequest(
                    List.of(new OrderInfoRequest(1L, 3))
            );

            inventoryService.decreaseProductInventoryIdempotent(KafkaTopic.ORDER_CREATED, orderId, request);
            inventoryService.decreaseProductInventoryIdempotent(KafkaTopic.ORDER_CREATED, orderId, request); // 중복 호출

            assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(7);
            assertThat(processedEventRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("increaseProductInventoryIdempotent - 멱등 재고 복구")
    class IncreaseProductInventoryIdempotentTest {

        @Test
        void 같은_orderId로_두_번_호출시_한_번만_복구() {
            inventoryRepository.save(Inventory.create(1L, 5));
            Long orderId = 200L;
            List<OrderItemInfo> items = List.of(new OrderItemInfo(1L, 3));

            inventoryService.increaseProductInventoryIdempotent(KafkaTopic.ORDER_FAILED, orderId, items);
            inventoryService.increaseProductInventoryIdempotent(KafkaTopic.ORDER_FAILED, orderId, items); // 중복 호출

            assertThat(inventoryRepository.findByProductId(1L).orElseThrow().getQuantity()).isEqualTo(8);
            assertThat(processedEventRepository.count()).isEqualTo(1);
        }
    }
}
