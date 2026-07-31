package com.mahesh.notificationservice.ai.repository;

import com.mahesh.notificationservice.ai.model.FailureAnalysisEntity;
import com.mahesh.notificationservice.model.NotificationDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FailureAnalysisRepository
        extends JpaRepository<FailureAnalysisEntity, Long> {
    Optional<FailureAnalysisEntity> findByNotificationId(Long notificationId);}
