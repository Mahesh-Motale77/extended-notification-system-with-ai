package com.mahesh.notificationservice.ai.service;

import com.mahesh.notificationservice.ai.dto.FailureAnalysisResponse;
import com.mahesh.notificationservice.model.NotificationDetails;

import java.io.IOException;

public interface FailureAnalysisService {

    FailureAnalysisResponse analyze(NotificationDetails notificationDetails);

}
