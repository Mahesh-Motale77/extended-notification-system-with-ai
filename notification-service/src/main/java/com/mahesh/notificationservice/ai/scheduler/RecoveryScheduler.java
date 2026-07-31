package com.mahesh.notificationservice.ai.scheduler;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import com.mahesh.notificationservice.ai.service.RecoveryExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecoveryScheduler {

    private final RecoveryDecisionRepository repository;
    private final RecoveryExecutorService recoveryExecutorService;

    @Scheduled(fixedRate = 600000)
    public void executeRecovery() {

        List<RecoveryDecisionEntity> pendingRecoveries =
                repository.findByStatus(
                        RecoveryDecisionEntity.Status.PENDING
                );

        pendingRecoveries.forEach(recoveryExecutorService::execute);
    }
}