package com.mahesh.managementapi.vo;

import com.mahesh.managementapi.model.NotificationDetails;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVo {

    private String orderId;

    @Enumerated(EnumType.STRING)
    private NotificationDetails.NotificationType oldStatus;

    @Enumerated(EnumType.STRING)
    private NotificationDetails.NotificationType newStatus;

    private LocalDateTime createdAt;

}
