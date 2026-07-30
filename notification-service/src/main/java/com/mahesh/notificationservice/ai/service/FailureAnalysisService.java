package com.mahesh.notificationservice.ai.service;

import com.mahesh.notificationservice.ai.dto.FailureAnalysisResponse;
import com.mahesh.notificationservice.model.NotificationDetails;

public interface FailureAnalysisService {

    FailureAnalysisResponse analyze(NotificationDetails notificationDetails);

}
