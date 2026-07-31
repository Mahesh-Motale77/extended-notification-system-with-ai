package com.mahesh.notificationservice.ai.tool;

import com.mahesh.notificationservice.ai.enums.RecoveryAction;
import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscalationTool implements RecoveryTool {

    private final RecoveryDecisionRepository repository;

    @Override
    public RecoveryAction getAction() {
        return RecoveryAction.ESCALATE;
    }

    @Override
    public void execute(RecoveryDecisionEntity decision) {

        log.error("Escalation required for Notification : {}",
                decision.getNotification().getId());

        decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

        repository.save(decision);

    }

}