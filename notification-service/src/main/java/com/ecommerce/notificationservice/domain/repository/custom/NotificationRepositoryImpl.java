package com.ecommerce.notificationservice.domain.repository.custom;

import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ecommerce.notificationservice.domain.entity.QNotification.notification;

@RequiredArgsConstructor
@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<NotificationListResponse> getNotificationPage(Long memberId, NotificationType notificationType, Pageable pageable) {
        List<NotificationListResponse> list = jpaQueryFactory.select(Projections.constructor(NotificationListResponse.class,
                        notification.id.as("id"),
                        notification.type.as("notificationType"),
                        notification.orderId.as("orderId"),
                        notification.paymentId.as("paymentId"),
                        notification.content.as("content"),
                        notification.isRead.as("isRead"),
                        notification.createdAt.as("createdAt")
                ))
                .from(notification)
                .where(notification.memberId.eq(memberId),
                        eqNotificationType(notificationType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory.select(notification.count())
                .from(notification)
                .where(notification.memberId.eq(memberId),
                        eqNotificationType(notificationType))
                .fetchOne();
        return new PageImpl<>(list, pageable, total);
    }

    private BooleanExpression eqNotificationType(NotificationType notificationType) {
        return notificationType == null ? null : notification.type.eq(notificationType);
    }
}
