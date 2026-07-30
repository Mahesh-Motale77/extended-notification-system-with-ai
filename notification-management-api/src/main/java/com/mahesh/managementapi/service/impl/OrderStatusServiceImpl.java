package com.mahesh.managementapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahesh.managementapi.dto.request.OrderStatusRequest;
import com.mahesh.managementapi.dto.response.OrderStatusResponse;
import com.mahesh.managementapi.exception.NotificationException;
import com.mahesh.managementapi.model.NotificationDetails;
import com.mahesh.managementapi.repository.NotificationDetailsRepository;
import com.mahesh.managementapi.service.OrderStatusService;
import com.mahesh.managementapi.vo.EventRequestVo;
import com.mahesh.managementapi.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static com.mahesh.managementapi.exception.ErrorCodes.ORDER_NOT_FOUND;
import static com.mahesh.managementapi.service.impl.DLQRetryServiceImpl.ORDER_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final NotificationDetailsRepository notificationDetailsRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public OrderStatusResponse changeStatus(OrderStatusRequest orderStatusRequest) throws JsonProcessingException {
        log.info("Inside OrderStatusServiceImpl -> changeStatus() : OrderStatusRequest : {}",orderStatusRequest);

        NotificationDetails notificationDetails = notificationDetailsRepository.findByOrderId(orderStatusRequest.getOrderId())
                .orElseThrow(()-> new NotificationException(ORDER_NOT_FOUND, "Order not found for orderId :"+ orderStatusRequest.getOrderId()));

        NotificationDetails.NotificationType oldStatus = notificationDetails.getNotificationType();

        EventRequestVo eventRequestVo = objectMapper.readValue(notificationDetails.getPayload(), EventRequestVo.class);
        eventRequestVo.setOrderStatus(orderStatusRequest.getNewStatus());
        eventRequestVo.setUpdatedAt(LocalDateTime.now());

        String updatedPayload = objectMapper.writeValueAsString(eventRequestVo);

        notificationDetails.setNotificationType(orderStatusRequest.getNewStatus());
        notificationDetails.setPayload(updatedPayload);

        notificationDetailsRepository.save(notificationDetails);

        CompletableFuture.runAsync(()->kafkaTemplate.send(ORDER_TOPIC, notificationDetails.getUserId(), notificationDetails.getPayload()));

        return OrderStatusResponse.builder()
                .statusCode("200")
                .message("Status changed for orderId :"+orderStatusRequest.getOrderId())
                .requestUUID(MDC.get("UUID"))
                .orderDetails(OrderVo.builder()
                        .orderId(orderStatusRequest.getOrderId())
                        .oldStatus(oldStatus)
                        .newStatus(orderStatusRequest.getNewStatus())
                        .createdAt(notificationDetails.getCreatedAt())
                        .build())
                .build();
    }

}
