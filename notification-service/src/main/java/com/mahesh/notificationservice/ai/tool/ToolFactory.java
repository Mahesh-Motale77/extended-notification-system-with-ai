package com.mahesh.notificationservice.ai.tool;

import com.mahesh.notificationservice.ai.enums.RecoveryAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ToolFactory {

    private final Map<RecoveryAction, RecoveryTool> toolMap;

    public ToolFactory(List<RecoveryTool> tools) {

        this.toolMap = tools.stream()
                .collect(Collectors.toMap(
                        RecoveryTool::getAction,
                        Function.identity()
                ));
    }

    public RecoveryTool getTool(RecoveryAction action) {
        return toolMap.get(action);
    }

}