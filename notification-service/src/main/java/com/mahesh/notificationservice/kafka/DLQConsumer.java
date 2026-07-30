package com.mahesh.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.dto.DLQMessage;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.repository.NotificationDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DLQConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationDetailsRepository notificationDetailsRepository;

    @KafkaListener(topics = "notification-dlq", groupId = "dlq-consumer-group")
    public void consumeDLQ(String message) {
        try {
            MDC.put("UUID", UUID.randomUUID().toString());

            DLQMessage dlqMessage = objectMapper.readValue(message, DLQMessage.class);

            log.error("DLQ ALERT | orderId : {} | userId : {} | channel : {} | retries : {} | reason : {}",
                    dlqMessage.getOrderId(),
                    dlqMessage.getUserId(),
                    dlqMessage.getChannel(),
                    dlqMessage.getRetryCount(),
                    dlqMessage.getErrorMessage()
            );

            // Update notification log status to DLQ
            notificationDetailsRepository.findByOrderId(dlqMessage.getOrderId())
                    .ifPresent(existingNotificationDetails -> {
                        existingNotificationDetails.setNotificationStatus(NotificationDetails.NotificationStatus.DLQ);
                        existingNotificationDetails.setErrorMessage(dlqMessage.getErrorMessage());
                        existingNotificationDetails.setUpdatedAt(LocalDateTime.now());
                        notificationDetailsRepository.save(existingNotificationDetails);

                        log.info("DLQ record saved | orderId : {}", dlqMessage.getOrderId());
                    });

        } catch (Exception e) {
            log.error("Failed to process DLQ message: {}", e.getMessage());
        }
    }
}
