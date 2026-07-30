package com.mahesh.notificationservice.ai.dto;

import com.mahesh.notificationservice.ai.enums.Severity;
import lombok.Data;

@Data
public class FailureAnalysisResponse {

    private String incidentSummary;
    private String rootCause;
    private Severity severity;
    private Boolean retryRecommended;
    private String suggestedFix;
    private Integer confidence;
}
