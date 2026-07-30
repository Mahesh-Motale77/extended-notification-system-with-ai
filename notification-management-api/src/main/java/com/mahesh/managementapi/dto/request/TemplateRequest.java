package com.mahesh.managementapi.dto.request;

import com.mahesh.managementapi.model.NotificationDetails;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class TemplateRequest {

    private NotificationDetails.Channel channel;
    private NotificationDetails.NotificationType notificationType;
    private String bodyTemplate;
    private String subject;
}
