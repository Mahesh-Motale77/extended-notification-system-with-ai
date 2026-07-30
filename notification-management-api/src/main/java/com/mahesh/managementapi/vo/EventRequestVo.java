package com.mahesh.managementapi.vo;

import com.mahesh.managementapi.model.NotificationDetails;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class EventRequestVo {

    private String orderId;
    private String userId;
    private String items;
    private Double amount;

    private NotificationDetails.NotificationType orderStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
