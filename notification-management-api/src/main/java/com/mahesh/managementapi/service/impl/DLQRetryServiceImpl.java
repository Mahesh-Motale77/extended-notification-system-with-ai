package com.mahesh.managementapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.managementapi.dto.response.DLQRetryResponse;
import com.mahesh.managementapi.exception.NotificationException;
import com.mahesh.managementapi.model.NotificationDetails;
import com.mahesh.managementapi.repository.NotificationDetailsRepository;
import com.mahesh.managementapi.service.DLQRetryService;
import com.mahesh.managementapi.vo.EventRequestVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

import static com.mahesh.managementapi.exception.ErrorCodes.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLQRetryServiceImpl implements DLQRetryService {

    private final NotificationDetailsRepository notificationDetailsRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    public static final String ORDER_TOPIC = "order-event-topic";

    public DLQRetryResponse retryDLQRecords(String orderId){
        log.info("Inside DLQRetryServiceImpl --> retryDLQRecords() : orderId : {} ", orderId);

        NotificationDetails notificationDetails = notificationDetailsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Notification details not found !"));

        if (!notificationDetails.getNotificationStatus().equals(NotificationDetails.NotificationStatus.DLQ)){
            throw new NotificationException(INVALID_STATUS , "Notification is not in DLQ status. | Current status: " + notificationDetails.getNotificationStatus());
        }

        if (Objects.isNull(notificationDetails.getPayload())){
            throw new NotificationException(NO_PAYLOAD , "No payload found for notification: " + orderId);
        }

        try {
            EventRequestVo eventRequestVo = objectMapper.readValue(notificationDetails.getPayload(), EventRequestVo.class);

            // Delete Redis idempotency key BEFORE retry
            String redisKey = IDEMPOTENCY_PREFIX + eventRequestVo.getOrderId() + ":" + eventRequestVo.getOrderStatus();

            redisTemplate.delete(redisKey);

            log.info("Cleared Redis key : {} for retry", redisKey);

            String payload = objectMapper.writeValueAsString(eventRequestVo);

            kafkaTemplate.send(ORDER_TOPIC, eventRequestVo.getUserId(), payload);

            log.info("DLQ retry triggered | orderId : {} | userId : {}",
                    orderId, eventRequestVo.getUserId());

            return DLQRetryResponse.builder()
                    .statusCode("200")
                    .errorMessage(null)
                    .requestUUID(MDC.get("UUID"))
                    .message("Retry triggered successfully for orderId : " + orderId)
                    .build();

        }catch (Exception e){
            log.error("DLQ retry failed | orderId : {} | error : {}", orderId, e.getMessage());
            throw new NotificationException(RETRY_FAILED, "DLQ retry failed: " + e.getMessage());
        }
    }
}
