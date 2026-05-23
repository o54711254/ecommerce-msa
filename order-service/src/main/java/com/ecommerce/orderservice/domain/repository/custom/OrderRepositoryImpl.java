package com.ecommerce.orderservice.domain.repository.custom;

import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ecommerce.orderservice.domain.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<OrderListResponse> getOrderList(Long memberId) {
        return jpaQueryFactory.select(Projections.constructor(OrderListResponse.class,
                        order.id.as("orderId"),
                        order.orderStatus.as("orderStatus"),
                        order.totalPrice.as("totalPrice"),
                        order.createdAt.as("createdAt")
                ))
                .from(order)
                .where(order.memberId.eq(memberId))
                .orderBy(order.createdAt.desc())
                .fetch();
    }
}
