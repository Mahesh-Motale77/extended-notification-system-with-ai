package com.mahesh.notificationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.channel.EmailDispacher;
import com.mahesh.notificationservice.dto.DLQMessage;
import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.repository.NotificationDetailsRepository;
import com.mahesh.notificationservice.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryServiceImpl implements RetryService {

    private final NotificationDetailsRepository notificationDetailsRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EmailDispacher emailDispacher;

    private static final int MAX_RETRIES = 3;
    private static final String DLQ_TOPIC = "notification-dlq";

    @Override
    public void handleFailure(EventRequest eventRequest,
                              NotificationDetails.Channel channel,
                              NotificationDetails notificationDetails,
                              String errorMessage) {

        int attempt = 0;
        String lastError = errorMessage;

        while (attempt < MAX_RETRIES) {
            try {
                long waitTime = (long) Math.pow(2, attempt + 1) * 1000;

                log.info("Retry attempt : {} | waiting : {}ms | orderId :{}",
                        attempt + 1, waitTime, eventRequest.getOrderId());

                Thread.sleep(waitTime);

                if (channel == NotificationDetails.Channel.EMAIL) {
                    emailDispacher.sendMail(eventRequest);
                }

                notificationDetails.setRetryCount(attempt + 1);
                notificationDetails.setNotificationStatus(
                        NotificationDetails.NotificationStatus.SENT);
                notificationDetails.setUpdatedAt(LocalDateTime.now());
                notificationDetailsRepository.save(notificationDetails);

                log.info("Retry succeeded | attempt : {} | orderId : {}",
                        attempt + 1, eventRequest.getOrderId());

                return;

            } catch (Exception e) {
                lastError = e.getMessage();
                attempt++;

                // Update retry count in DB
                notificationDetails.setRetryCount(attempt);
                notificationDetails.setErrorMessage(lastError);
                notificationDetails.setNotificationStatus(
                        NotificationDetails.NotificationStatus.FAILED);
                notificationDetails.setUpdatedAt(LocalDateTime.now());
                notificationDetailsRepository.save(notificationDetails);

                log.error("Retry attempt : {} failed | orderId : {} | error : {}",
                        attempt, eventRequest.getOrderId(), lastError);
            }
        }

        // All retries exhausted → send to DLQ
        log.error("All {} retries exhausted | orderId : {}", MAX_RETRIES, eventRequest.getOrderId());

        sendToDLQ(eventRequest, channel, notificationDetails, lastError);
    }

    @Override
    public void sendToDLQ(EventRequest event,
                          NotificationDetails.Channel channel,
                          NotificationDetails notificationDetails,
                          String errorMessage) {
        try {
            DLQMessage dlqMessage = DLQMessage.builder()
                    .orderId(event.getOrderId())
                    .orderTopic("order-event-topic")
                    .notificationType(event.getOrderStatus())
                    .userId(event.getUserId())
                    .channel(channel.name())
                    .retryCount(notificationDetails.getRetryCount())
                    .errorMessage(errorMessage)
                    .failedAt(LocalDateTime.now())
                    .originalPayload(event)
                    .build();

            // Publish to DLQ topic
            String message = objectMapper.writeValueAsString(dlqMessage);
            kafkaTemplate.send(DLQ_TOPIC, String.valueOf(event.getUserId()), message);

            log.error("Pushed to DLQ | orderId : {} | userId : {} | reason : {}",
                    event.getOrderId(),
                    event.getUserId(),
                    errorMessage);

        } catch (Exception e) {
            log.error("Failed to push to DLQ | error={}", e.getMessage());
        }
    }
}
