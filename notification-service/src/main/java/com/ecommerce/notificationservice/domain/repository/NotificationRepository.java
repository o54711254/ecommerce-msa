package com.ecommerce.notificationservice.domain.repository;

import com.ecommerce.notificationservice.domain.entity.Notification;
import com.ecommerce.notificationservice.domain.repository.custom.NotificationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
}
