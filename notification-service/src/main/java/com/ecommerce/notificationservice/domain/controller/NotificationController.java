package com.ecommerce.notificationservice.domain.controller;

import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationListResponse>> getNotificationList(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestParam(required = false) NotificationType notificationType,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationList(memberId, notificationType, pageable));
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Void> readNotification(@RequestHeader("X-Member-Id") Long memberId,
                                                 @PathVariable Long id) {
        notificationService.readNotification(memberId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@RequestHeader("X-Member-Id") Long memberId,
                                                   @PathVariable Long id) {
        notificationService.deleteNotification(memberId, id);
        return ResponseEntity.ok().build();
    }
}
