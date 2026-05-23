package com.ecommerce.inventoryservice.domain.service;

import com.ecommerce.inventoryservice.domain.dto.req.CreateInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.req.DecreaseProductInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.req.OrderInfoRequest;
import com.ecommerce.inventoryservice.domain.dto.req.UpdateInventoryRequest;
import com.ecommerce.inventoryservice.domain.dto.res.InventoryResponse;
import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.ecommerce.inventoryservice.domain.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public Long createInventory(CreateInventoryRequest request) {
        Inventory inventory = Inventory.create(request.productId(), request.quantity());
        return inventoryRepository.save(inventory).getId();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        Inventory inventory = getInventory(productId);
        return new InventoryResponse(inventory.getProductId(), inventory.getQuantity());
    }

    @Transactional
    public void deleteInventoryByProductId(Long productId) {
        inventoryRepository.deleteByProductId(productId);
    }

    @Transactional
    public void addProductInventory(Long productId, UpdateInventoryRequest request) {
        Inventory inventory = getInventory(productId);
        inventory.addQuantity(request.quantity());
    }

    @Transactional
    public void decreaseProductInventory(DecreaseProductInventoryRequest request) {
        List<OrderInfoRequest> list = request.orderInfoRequest();
        List<Long> productIds = list.stream().map(OrderInfoRequest::productId).toList();
        Map<Long, Integer> eaMap = list.stream().collect(Collectors.toMap(OrderInfoRequest::productId, OrderInfoRequest::quantity));

        List<Inventory> inventoryList = inventoryRepository.findInventoriesByProductIdForLock(productIds);
        for(Inventory inventory : inventoryList) {
            inventory.decreaseQuantity(eaMap.get(inventory.getProductId()));
        }
    }

    private Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
