package com.mahesh.managementapi.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class NotificationDetailsVo {

    private String orderId;
    private String userId;

    private String items;
    private String amount;

    private String notificationStatus;
    private String notificationType;
    private String channel;

    private Integer retryCount;
    private String errorMessage;
    private EventRequestVo payload;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
