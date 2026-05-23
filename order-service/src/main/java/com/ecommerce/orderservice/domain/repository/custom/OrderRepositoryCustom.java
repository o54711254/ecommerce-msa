package com.ecommerce.orderservice.domain.repository.custom;

import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;

import java.util.List;

public interface OrderRepositoryCustom {

    List<OrderListResponse> getOrderList(Long memberId);
}
