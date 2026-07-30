package com.mahesh.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.redis.IdempotencyService;
import com.mahesh.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;


    @KafkaListener(topics = "order-event-topic", groupId = "notification-group")
    private void consumeOrderEvent(String kafkaRequest){
        try {
            MDC.put("UUID", UUID.randomUUID().toString());

            EventRequest eventRequest = objectMapper.readValue(kafkaRequest, EventRequest.class);

            log.info("Order Notification Event received for orderId : {} | userId : {} | orderStatus : {}",
                    eventRequest.getOrderId(), eventRequest.getUserId(), eventRequest.getOrderStatus());

            if(idempotencyService.isAlreadyProcessed(eventRequest)){
                log.info("Duplicate event for notification detected | OrderId : {} - Skipping",eventRequest.getOrderId());
                return;
            }

            notificationService.processForNotification(eventRequest);

            // marked event as processed in redis
            idempotencyService.markAsProcessed(eventRequest);

        }catch (Exception e){
            log.info("Exception occurred while consuming order notification event : {}", e.getMessage());
        }

    }

}
