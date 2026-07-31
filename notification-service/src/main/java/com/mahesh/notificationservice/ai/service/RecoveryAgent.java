package com.mahesh.notificationservice.ai.service;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;

public interface RecoveryAgent {

    void execute(RecoveryDecisionEntity decision);

}