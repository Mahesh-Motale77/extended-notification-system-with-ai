package com.mahesh.notificationservice.service;

import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;

public interface RetryService {

    void handleFailure(EventRequest eventRequest,
                              NotificationDetails.Channel channel,
                              NotificationDetails notificationDetails,
                              String errorMessage);

    void sendToDLQ(EventRequest event,
                   NotificationDetails.Channel channel,
                   NotificationDetails notificationDetails,
                   String errorMessage);
}
