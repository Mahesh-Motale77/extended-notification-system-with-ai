package com.mahesh.notificationservice.ai.repository;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecoveryDecisionRepository extends JpaRepository<RecoveryDecisionEntity, Long> {

    @Query("""
        SELECT r
        FROM RecoveryDecisionEntity r
        JOIN FETCH r.notification
        WHERE r.status = :status
        """)
    List<RecoveryDecisionEntity> findByStatusWithNotification(
            @Param("status") RecoveryDecisionEntity.Status status
    );
}