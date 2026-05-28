package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.client.inventory.InventoryClient;
import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
import com.ecommerce.orderservice.client.inventory.dto.OrderInfoRequest;
import com.ecommerce.orderservice.client.payment.PaymentClient;
import com.ecommerce.orderservice.client.payment.dto.req.CreatePaymentRequest;
import com.ecommerce.orderservice.client.product.ProductClient;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderItemRequest;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderRequest;
import com.ecommerce.orderservice.domain.dto.res.OrderItemResponse;
import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;
import com.ecommerce.orderservice.domain.dto.res.OrderResponse;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.global.exception.custom.OrderAccessDeniedException;
import com.ecommerce.orderservice.global.exception.custom.OrderNotFoundException;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.orderservice.kafka.dto.OrderItemInfo;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
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
    private final PaymentClient paymentClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrderList(Long memberId) {
        return orderRepository.getOrderList(memberId);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long memberId, Long id) {
        Order order = getOrder(id);
        if (!order.getMemberId().equals(memberId)) {
            throw new OrderAccessDeniedException();
        }
        List<OrderItem> orderItems = order.getOrderItems();

        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        Map<Long, String> productNames = productClient.getNamesMap(productIds).getBody().nameMap();

        List<OrderItemResponse> orderItemResponses = orderItems.stream()
                .map(item -> {
                    int quantity = item.getQuantity();
                    Long price = item.getItemPrice();
                    return OrderItemResponse.builder()
                            .orderItemId(item.getId())
                            .productId(item.getProductId())
                            .productName(productNames.get(item.getProductId()))
                            .quantity(quantity)
                            .itemPrice(price)
                            .totalPrice(quantity * price)
                            .build();
                })
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .itemList(orderItemResponses)
                .build();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = getOrder(orderId);
        order.updateStatus(orderStatus);
    }

    @Transactional(readOnly = true)
    public void orderFailed(Long orderId) {
        Order order = getOrder(orderId);
        List<OrderItemInfo> itemInfoList = order.getOrderItems().stream()
                .map(item -> new OrderItemInfo(item.getProductId(), item.getQuantity()))
                .toList();

        orderEventProducer.sendOrderFailed(new OrderFailedEvent(orderId, itemInfoList));
    }


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
        orderRepository.save(order);

        // 결제 생성
        paymentClient.createPayment(memberId, new CreatePaymentRequest(order.getId(), order.getTotalPrice()));

        return order.getId();
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }
}
