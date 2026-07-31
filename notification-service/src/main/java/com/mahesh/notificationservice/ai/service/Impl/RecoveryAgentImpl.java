package com.mahesh.notificationservice.ai.service.Impl;

import com.mahesh.notificationservice.ai.model.RecoveryDecisionEntity;
import com.mahesh.notificationservice.ai.service.RecoveryAgent;
import com.mahesh.notificationservice.ai.tool.ToolFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RecoveryAgentImpl implements RecoveryAgent {

    private final ToolFactory toolFactory;

    @Override
    public void execute(RecoveryDecisionEntity decision) {

        toolFactory
                .getTool(decision.getRecoveryAction())
                .execute(decision);

    }

}