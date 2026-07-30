package com.mahesh.notificationservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
public class DLQMessage {
    private String orderId;
    private String orderTopic;
    private String notificationType;
    private String userId;
    private String channel;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime failedAt;
    private EventRequest originalPayload;
}
