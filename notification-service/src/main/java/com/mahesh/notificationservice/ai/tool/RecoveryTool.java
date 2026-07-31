package com.mahesh.notificationservice.ai.tool;

import com.mahesh.notificationservice.ai.enums.RecoveryAction;
import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;

public interface RecoveryTool {

    RecoveryAction getAction();

    void execute(RecoveryDecisionEntity decision);

}