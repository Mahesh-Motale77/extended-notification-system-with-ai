package com.mahesh.notificationservice.service;

import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;

public interface TemplateService {

    String buildMessage(EventRequest eventRequest, NotificationDetails.Channel channel);

    String buildSubject(EventRequest eventRequest, NotificationDetails.Channel channel);

}
