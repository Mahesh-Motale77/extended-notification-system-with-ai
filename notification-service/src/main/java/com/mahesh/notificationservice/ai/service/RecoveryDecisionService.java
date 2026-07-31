package com.mahesh.notificationservice.ai.service;

import com.mahesh.notificationservice.ai.dto.RecoveryDecisionResponse;
import com.mahesh.notificationservice.model.NotificationDetails;

public interface RecoveryDecisionService {

    RecoveryDecisionResponse decide(NotificationDetails notification);

}
