package com.ecommerce.orderservice.domain.controller;

import com.ecommerce.orderservice.domain.dto.req.CreateOrderRequest;
import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;
import com.ecommerce.orderservice.domain.dto.res.OrderResponse;
import com.ecommerce.orderservice.domain.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestHeader("X-Member-Id") Long memberId,
                                            @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderService.createOrder(memberId, createOrderRequest));
    }

    @GetMapping
    public ResponseEntity<List<OrderListResponse>> getOrderList(@RequestHeader("X-Member-Id") Long memberId) {
        return ResponseEntity.ok(orderService.getOrderList(memberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderDetail(@RequestHeader("X-Member-Id") Long memberId,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetail(memberId, id));
    }
}
