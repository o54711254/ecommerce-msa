package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.client.inventory.InventoryClient;
import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
import com.ecommerce.orderservice.client.inventory.dto.OrderInfoRequest;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderItemRequest;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderRequest;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.client.product.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public Long createOrder(Long memberId, CreateOrderRequest request) {
        List<CreateOrderItemRequest> items = request.items();

        List<Long> productIds = items.stream()
                .map(CreateOrderItemRequest::productId)
                .toList();

        // 가격 조회
        Map<Long, Long> priceMap = productClient.getPriceMap(productIds).getBody().priceMap();

        // 재고 차감
        List<OrderInfoRequest> orderInfoRequests = items.stream()
                .map(item -> new OrderInfoRequest(item.productId(), item.quantity()))
                .toList();
        inventoryClient.decreaseInventory(new DecreaseProductInventoryRequest(orderInfoRequests));

        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.create(item.productId(), item.quantity(), priceMap.get(item.productId())))
                .toList();

        Order order = Order.create(memberId, orderItems);
        return orderRepository.save(order).getId();
    }
}
