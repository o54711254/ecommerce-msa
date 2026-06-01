package com.ecommerce.inventoryservice.domain.entity;

import com.ecommerce.inventoryservice.global.exception.custom.InvalidQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Nested
    @DisplayName("addQuantity - 재고 증가")
    class AddQuantityTest {

        @Test
        void 성공() {
            Inventory inventory = Inventory.create(1L, 10);

            inventory.addQuantity(5);

            assertThat(inventory.getQuantity()).isEqualTo(15);
        }

        @Test
        void 실패_음수_입력() {
            Inventory inventory = Inventory.create(1L, 10);

            assertThatThrownBy(() -> inventory.addQuantity(-1))
                    .isInstanceOf(InvalidQuantityException.class);
        }

        @Test
        void 실패_0_입력() {
            Inventory inventory = Inventory.create(1L, 10);

            assertThatThrownBy(() -> inventory.addQuantity(0))
                    .isInstanceOf(InvalidQuantityException.class);
        }
    }
}
