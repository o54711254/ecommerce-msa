package com.ecommerce.inventoryservice.domain.service;

import com.ecommerce.inventoryservice.domain.dto.req.CreateInventoryRequest;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.ecommerce.inventoryservice.domain.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
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
        // given
        CreateInventoryRequest request = new CreateInventoryRequest(1L, 10);
        Inventory savedInventory = Inventory.create(request.productId(), request.quantity());
        ReflectionTestUtils.setField(savedInventory, "id", 1L);
        given(inventoryRepository.save(any(Inventory.class))).willReturn(savedInventory);

        // when
        Long result = inventoryService.createInventory(request);

        // then
        assertThat(result).isEqualTo(1L);
        verify(inventoryRepository).save(any(Inventory.class));
    }
}
