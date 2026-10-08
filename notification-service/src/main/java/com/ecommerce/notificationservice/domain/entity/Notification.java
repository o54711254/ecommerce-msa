package com.ecommerce.notificationservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private ReferenceType referenceType;

    @Column(nullable = false)
    private Long referenceId;

    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    public static Notification create(Long memberId, NotificationType type, Long referenceId) {
        Notification notification = new Notification();
        notification.memberId = memberId;
        notification.type = type;
        notification.referenceType = type.getReferenceType();
        notification.referenceId = referenceId;
        notification.content = String.format(type.getMessage(), referenceId);
        notification.isRead = false;
        return notification;
    }

    public void read(){
        this.isRead = true;
    }
}
