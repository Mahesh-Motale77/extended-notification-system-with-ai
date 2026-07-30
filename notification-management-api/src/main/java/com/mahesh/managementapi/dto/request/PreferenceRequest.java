package com.mahesh.managementapi.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class PreferenceRequest {
    private String userId;
    private String channel;
    private String notificationType;
    private Boolean isEnable;
}
