package com.mahesh.notificationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.channel.EmailDispacher;
import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.model.NotificationPreferences;
import com.mahesh.notificationservice.repository.NotificationDetailsRepository;
import com.mahesh.notificationservice.repository.NotificationPreferencesRepository;
import com.mahesh.notificationservice.service.NotificationService;
import com.mahesh.notificationservice.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final NotificationDetailsRepository notificationDetailsRepository;
    private final RetryService retryService;
    private final EmailDispacher emailDispacher;
    private final ObjectMapper objectMapper;

    @Override
    public void processForNotification(EventRequest eventRequest) {
        log.info("Inside NotificationServiceImpl-->  processForNotification() : EventRequest : {}", eventRequest);

        List<NotificationPreferences> notificationPreferences = notificationPreferencesRepository.findByUserId(eventRequest.getUserId());

        if (notificationPreferences.isEmpty()){
            log.info("No preferences found for userId : {} | Defaulting to email", eventRequest.getUserId());
            sendMailAndSaveDetails(eventRequest, NotificationDetails.Channel.EMAIL);
            return;
        }

        for (NotificationPreferences preference : notificationPreferences) {

            if (!preference.getIsEnable()) {
                log.info("Notifications disabled for userId={} | notificationType={}",
                        eventRequest.getUserId(), eventRequest.getOrderStatus());
                continue;
            }

            switch (preference.getChannel()) {
                case EMAIL -> sendMailAndSaveDetails(eventRequest, NotificationDetails.Channel.EMAIL);
                case SMS -> log.info("SMS dispatcher coming soon");
                case BOTH -> {
                    sendMailAndSaveDetails(eventRequest, NotificationDetails.Channel.EMAIL);
                    log.info("SMS dispatcher coming soon");
                }
            }
        }
    }

    private void sendMailAndSaveDetails(EventRequest eventRequest, NotificationDetails.Channel channel) {
        log.info("Inside NotificationServiceImpl-->  sendMailAndSaveDetails() : EventRequest : {}", eventRequest);

        String jsonRequest;
        try {
            jsonRequest = objectMapper.writeValueAsString(eventRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        NotificationDetails notificationDetails = notificationDetailsRepository.findByOrderId(eventRequest.getOrderId())
                .map(existing ->
                        {
                            existing.setNotificationStatus(NotificationDetails.NotificationStatus.PENDING);
                            existing.setRetryCount(0);
                            existing.setErrorMessage(null);
                            existing.setUpdatedAt(LocalDateTime.now());
                            log.info("Reusing existing notification details | orderId : {}",
                                    eventRequest.getOrderId());
                            return existing;
                        })
                .orElseGet(() -> {
                            log.info("Creating new notification entry | orderId : {}", eventRequest.getOrderId());
                            return NotificationDetails.builder()
                                    .userId(eventRequest.getUserId())
                                    .orderId(eventRequest.getOrderId())
                                    .notificationType(NotificationDetails.NotificationType.valueOf(eventRequest.getOrderStatus()))
                                    .notificationStatus(NotificationDetails.NotificationStatus.PENDING)
                                    .channel(channel)
                                    .retryCount(0)
                                    .payload(jsonRequest)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                        }
                );

        NotificationDetails savedNotificationDetails = notificationDetailsRepository.save(notificationDetails);
        log.info("Inside NotificationServiceImpl-->  sendMailAndSaveDetails() : savedNotificationDetails : {}", savedNotificationDetails);

        try {
            emailDispacher.sendMail(eventRequest);

            notificationDetails.setNotificationStatus(NotificationDetails.NotificationStatus.SENT);
            notificationDetails.setUpdatedAt(LocalDateTime.now());
            notificationDetailsRepository.save(notificationDetails);
        } catch (Exception e) {
            notificationDetails.setNotificationStatus(NotificationDetails.NotificationStatus.FAILED);
            notificationDetails.setErrorMessage(e.getMessage());
            notificationDetails.setUpdatedAt(LocalDateTime.now());
            notificationDetailsRepository.save(notificationDetails);

            log.error("Notification failed for userId={} | error={}",
                    eventRequest.getUserId(), e.getMessage());

            // Get existing log and send to retry
            notificationDetailsRepository.findByOrderId(eventRequest.getOrderId())
                    .ifPresent(existingNotificationDetails ->
                            retryService.handleFailure(eventRequest, channel, existingNotificationDetails, e.getMessage()));
        }
    }

}
