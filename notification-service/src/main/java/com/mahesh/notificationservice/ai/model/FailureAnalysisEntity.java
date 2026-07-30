package com.mahesh.notificationservice.ai.model;

import com.mahesh.notificationservice.ai.enums.Severity;
import com.mahesh.notificationservice.model.NotificationDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private NotificationDetails notification;

    @Lob
    private String incidentSummary;

    @Lob
    private String rootCause;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private Boolean retryRecommended;

    @Lob
    private String suggestedFix;

    private Integer confidence;

    private String aiModel;

    private LocalDateTime analyzedAt;
}