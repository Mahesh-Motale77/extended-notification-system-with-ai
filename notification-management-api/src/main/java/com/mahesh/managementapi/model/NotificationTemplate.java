package com.mahesh.managementapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "notification_template")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private NotificationDetails.NotificationType notificationType;

    @Enumerated(value = EnumType.STRING)
    private NotificationDetails.Channel channel;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String bodyTemplate;
}
