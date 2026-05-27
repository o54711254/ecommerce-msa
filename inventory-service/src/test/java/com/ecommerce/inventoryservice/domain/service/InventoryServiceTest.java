package com.ecommerce.inventoryservice.domain.service;

import com.ecommerce.inventoryservice.domain.dto.req.CreateInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.req.UpdateInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.res.InventoryResponse;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.ecommerce.inventoryservice.domain.repository.InventoryRepository;
import com.ecommerce.inventoryservice.global.exception.custom.InventoryNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void 재고_등록_성공() {
        CreateInventoryRequest request = new CreateInventoryRequest(1L, 10);
        Inventory savedInventory = Inventory.create(request.productId(), request.quantity());
        ReflectionTestUtils.setField(savedInventory, "id", 1L);
        given(inventoryRepository.save(any(Inventory.class))).willReturn(savedInventory);

        Long result = inventoryService.createInventory(request);

        assertThat(result).isEqualTo(1L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void 재고_단건_조회_성공() {
        Inventory inventory = Inventory.create(1L, 10);
        given(inventoryRepository.findByProductId(1L)).willReturn(Optional.of(inventory));

        InventoryResponse response = inventoryService.getInventoryByProductId(1L);

        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.quantity()).isEqualTo(10);
    }

    @Test
    void 재고_단건_조회_실패_상품없음() {
        given(inventoryRepository.findByProductId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProductId(1L))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void 재고_삭제_성공() {
        inventoryService.deleteInventoryByProductId(1L);

        verify(inventoryRepository).deleteByProductId(1L);
    }

    @Test
    void 재고_증가_성공() {
        Inventory inventory = Inventory.create(1L, 10);
        given(inventoryRepository.findByProductId(1L)).willReturn(Optional.of(inventory));

        inventoryService.addProductInventory(1L, new UpdateInventoryRequest(5));

        assertThat(inventory.getQuantity()).isEqualTo(15);
    }

    @Test
    void 재고_증가_실패_상품없음() {
        given(inventoryRepository.findByProductId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.addProductInventory(1L, new UpdateInventoryRequest(5)))
                .isInstanceOf(InventoryNotFoundException.class);
    }
}
