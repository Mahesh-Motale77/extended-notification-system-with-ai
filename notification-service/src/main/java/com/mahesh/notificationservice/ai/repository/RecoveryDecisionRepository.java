package com.mahesh.notificationservice.ai.repository;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecoveryDecisionRepository extends JpaRepository<RecoveryDecisionEntity, Long> {
    List<RecoveryDecisionEntity> findByStatus(
            RecoveryDecisionEntity.Status status
    );
}