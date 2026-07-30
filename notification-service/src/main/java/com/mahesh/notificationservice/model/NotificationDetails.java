package com.mahesh.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String userId;

    @Enumerated(value = EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(value = EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Enumerated(value = EnumType.STRING)
    private Channel channel;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private Integer retryCount;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Channel{
        SMS, EMAIL
    }

    public enum NotificationStatus{
        PENDING, SENT, FAILED, DLQ
    }

    public enum NotificationType{
        ORDER_CREATED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, PAYMENT_SUCCESS, PAYMENT_FAILED
    }
}
