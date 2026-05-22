package com.ecommerce.inventoryservice.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Test
    void 재고_증가_성공() {
        Inventory inventory = Inventory.create(1L, 10);

        inventory.addQuantity(5);

        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    void 재고_증가_실패_음수() {
        Inventory inventory = Inventory.create(1L, 10);

        assertThatThrownBy(() -> inventory.addQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 재고_증가_실패_0() {
        Inventory inventory = Inventory.create(1L, 10);

        assertThatThrownBy(() -> inventory.addQuantity(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
