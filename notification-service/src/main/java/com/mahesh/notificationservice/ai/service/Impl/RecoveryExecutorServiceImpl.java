package com.mahesh.notificationservice.ai.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.repository.RecoveryDecisionRepository;
import com.mahesh.notificationservice.ai.service.RecoveryExecutorService;
import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryExecutorServiceImpl implements RecoveryExecutorService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final RecoveryDecisionRepository repository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Override
    public void execute(RecoveryDecisionEntity decision) {
        switch (decision.getRecoveryAction()) {

            case RETRY_NOW, RETRY_LATER -> retry(decision);

            case ESCALATE -> escalate(decision);

            case MANUAL_REVIEW -> manualReview(decision);

            case DISCARD -> discard(decision);
        }
    }

    private void retry(RecoveryDecisionEntity decision) {

        NotificationDetails notification = decision.getNotification();

        try {
            EventRequest request =
                    objectMapper.readValue(
                            notification.getPayload(),
                            EventRequest.class);

            notificationService.processForNotification(request);

            decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

            repository.save(decision);

            log.info("Retry initiated for orderId : {}", notification.getOrderId());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private void escalate(RecoveryDecisionEntity decision) {

        log.error("Escalation required for Notification : {}",
                decision.getNotification().getId());

        decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

        repository.save(decision);

    }

    private void manualReview(RecoveryDecisionEntity decision) {

        log.warn("Manual review required for Notification : {}",
                decision.getNotification().getId());

        decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

        repository.save(decision);

    }

    private void discard(RecoveryDecisionEntity decision) {

        log.info("Notification discarded : {}",
                decision.getNotification().getId());

        decision.setStatus(RecoveryDecisionEntity.Status.COMPLETED);

        repository.save(decision);

    }

}