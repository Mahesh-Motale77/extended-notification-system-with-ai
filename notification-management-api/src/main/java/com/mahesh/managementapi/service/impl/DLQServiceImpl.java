package com.mahesh.managementapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.managementapi.dto.response.NotificationDetailsResponse;
import com.mahesh.managementapi.exception.NotificationException;
import com.mahesh.managementapi.model.NotificationDetails;
import com.mahesh.managementapi.repository.NotificationDetailsRepository;
import com.mahesh.managementapi.service.DLQService;
import com.mahesh.managementapi.vo.EventRequestVo;
import com.mahesh.managementapi.vo.NotificationDetailsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mahesh.managementapi.exception.ErrorCodes.DLQ_NOT_FOUND;
import static com.mahesh.managementapi.model.NotificationDetails.NotificationStatus.DLQ;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLQServiceImpl implements DLQService {

    private final NotificationDetailsRepository notificationDetailsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public NotificationDetailsResponse getAllDlqNotifications() {
        log.info("Inside DLQServiceImpl --> getAllDlqNotifications()");

        List<NotificationDetails> notificationDetails = notificationDetailsRepository.findByNotificationStatus(DLQ);

        if (notificationDetails.isEmpty()){
            throw new NotificationException(DLQ_NOT_FOUND, "Notifications not found with DLQ status");
        }

        List<NotificationDetailsVo> notificationDetailsVo = notificationDetails
                .stream()
                .map(this::mapToNotificationDetailVo)
                .toList();

        return NotificationDetailsResponse.builder()
                .statusCode("200")
                .message(null)
                .requestUUID(MDC.get("UUID"))
                .build();
    }

    private NotificationDetailsVo mapToNotificationDetailVo(NotificationDetails details) {
        EventRequestVo payload;
        try {
            payload = objectMapper.readValue(details.getPayload(), EventRequestVo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return NotificationDetailsVo.builder()
                .orderId(details.getOrderId())
                .userId(details.getUserId())
                .items(details.getItems())
                .amount(details.getAmount())
                .channel(String.valueOf(details.getChannel()))
                .notificationStatus(String.valueOf(details.getNotificationStatus()))
                .notificationType(String.valueOf(details.getNotificationType()))
                .payload(payload)
                .errorMessage(details.getErrorMessage())
                .retryCount(details.getRetryCount())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }
}
