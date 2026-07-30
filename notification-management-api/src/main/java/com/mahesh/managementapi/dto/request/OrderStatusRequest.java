package com.mahesh.managementapi.dto.request;

import com.mahesh.managementapi.model.NotificationDetails;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
public class OrderStatusRequest {

    private String orderId;
    private NotificationDetails.NotificationType newStatus;
}
