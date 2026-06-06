package com.ecommerce.notificationservice.domain.repository;

import com.ecommerce.notificationservice.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
