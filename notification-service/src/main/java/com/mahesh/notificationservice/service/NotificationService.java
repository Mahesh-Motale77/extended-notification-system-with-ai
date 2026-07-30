package com.mahesh.notificationservice.service;

import com.mahesh.notificationservice.dto.EventRequest;

public interface NotificationService {

    void processForNotification(EventRequest eventRequest);
}
