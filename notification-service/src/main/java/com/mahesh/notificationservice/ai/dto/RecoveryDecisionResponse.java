package com.mahesh.notificationservice.ai.dto;

import com.mahesh.notificationservice.ai.enums.RecoveryAction;

public record RecoveryDecisionResponse(
        RecoveryAction recoveryAction,
        Integer retryAfterMinutes,
        String reason,
        Integer confidence
) { }