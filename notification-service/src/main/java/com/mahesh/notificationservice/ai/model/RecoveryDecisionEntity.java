package com.mahesh.notificationservice.ai.model;

import com.mahesh.notificationservice.ai.enums.RecoveryAction;
import com.mahesh.notificationservice.model.NotificationDetails;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_decision")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationDetails notification;

    @Enumerated(EnumType.STRING)
    private RecoveryAction recoveryAction;

    private Integer retryAfterMinutes;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private Integer confidence;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime nextRetryAt;

    private LocalDateTime createdAt;

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }
}