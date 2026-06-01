package com.ecommerce.inventoryservice.domain.repository;

import com.ecommerce.inventoryservice.AbstractIntegrationTest;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private InventoryRepository inventoryRepository;

    @Nested
    @Transactional
    @DisplayName("findInventoriesByProductIdForLock - 비관적 락을 이용한 재고 일괄 조회")
    class FindInventoriesByProductIdForLockTest {

        @BeforeEach
        void setUp() {
            inventoryRepository.deleteAll();
            inventoryRepository.flush();
        }

        @Test
        void 요청한_productId만_반환() {
            inventoryRepository.saveAll(List.of(
                    Inventory.create(1L, 10),
                    Inventory.create(2L, 20),
                    Inventory.create(3L, 30)
            ));

            List<Inventory> result = inventoryRepository.findInventoriesByProductIdForLock(List.of(1L, 3L));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Inventory::getProductId).containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        void 존재하지_않는_productId는_제외() {
            inventoryRepository.save(Inventory.create(1L, 10));

            List<Inventory> result = inventoryRepository.findInventoriesByProductIdForLock(List.of(1L, 999L));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductId()).isEqualTo(1L);
        }

        @Test
        void productId_오름차순_정렬() {
            // 데드락 방지를 위해 락 획득 순서가 항상 productId asc여야 함
            inventoryRepository.saveAll(List.of(
                    Inventory.create(3L, 30),
                    Inventory.create(1L, 10),
                    Inventory.create(2L, 20)
            ));

            List<Inventory> result = inventoryRepository.findInventoriesByProductIdForLock(List.of(1L, 2L, 3L));

            assertThat(result).extracting(Inventory::getProductId)
                    .containsExactly(1L, 2L, 3L);
        }
    }
}
