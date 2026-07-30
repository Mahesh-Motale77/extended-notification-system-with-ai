package com.mahesh.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Enumerated(value = EnumType.STRING)
    private NotificationDetails.NotificationType notificationType;

    @Enumerated(value = EnumType.STRING)
    private Channel channel;

    private Boolean isEnable;

    public enum Channel{
        SMS, EMAIL, BOTH
    }

}
