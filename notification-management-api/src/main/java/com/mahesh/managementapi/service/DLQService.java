package com.mahesh.managementapi.service;

import com.mahesh.managementapi.dto.response.NotificationDetailsResponse;

public interface DLQService {
    NotificationDetailsResponse getAllDlqNotifications();
}
