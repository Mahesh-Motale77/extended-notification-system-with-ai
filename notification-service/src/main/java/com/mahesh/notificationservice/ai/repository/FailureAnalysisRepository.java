package com.mahesh.notificationservice.ai.repository;

import com.mahesh.notificationservice.ai.model.FailureAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureAnalysisRepository
        extends JpaRepository<FailureAnalysisEntity, Long> {

}
