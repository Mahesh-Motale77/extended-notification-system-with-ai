package com.mahesh.notificationservice.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.ai.enums.RecoveryAction;
import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryTool implements RecoveryTool {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final RecoveryDecisionRepository repository;

    @Override
    public RecoveryAction getAction() {
        return RecoveryAction.RETRY_NOW;
    }

    @Override
    public void execute(RecoveryDecisionEntity decision) {

        if (decision.getRecoveryAction() == RecoveryAction.RETRY_LATER &&
                decision.getNextRetryAt().isAfter(java.time.LocalDateTime.now())) {
            return;
        }

        NotificationDetails notification = decision.getNotification();

        try {

            EventRequest request = objectMapper.readValue(
                    notification.getPayload(),
                    EventRequest.class);

            notificationService.processForNotification(request);

            decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

            repository.save(decision);

            log.info("Retry initiated for Order : {}", notification.getOrderId());

        } catch (Exception e) {

            decision.setStatus(RecoveryDecisionEntity.Status.FAILED);

            repository.save(decision);

            throw new RuntimeException(e);
        }

    }

}