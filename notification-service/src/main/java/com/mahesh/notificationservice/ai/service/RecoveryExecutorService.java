package com.mahesh.notificationservice.ai.service;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;

public interface RecoveryExecutorService {

    void execute(RecoveryDecisionEntity decision);

}