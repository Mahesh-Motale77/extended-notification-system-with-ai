package com.mahesh.notificationservice.ai.scheduler;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import com.mahesh.notificationservice.ai.service.RecoveryAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecoveryScheduler {

    private final RecoveryDecisionRepository repository;
    private final RecoveryAgent recoveryAgent;

    @Scheduled(fixedRate = 600000)
    public void executeRecovery() {

        List<RecoveryDecisionEntity> pendingRecoveries =
                repository.findByStatusWithNotification(
                        RecoveryDecisionEntity.Status.PENDING
                );

        pendingRecoveries.forEach(recoveryAgent::execute);
    }
}