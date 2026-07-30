package com.mahesh.managementapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DLQRetryResponse {
    private String statusCode;
    private String errorMessage;
    private String requestUUID;
    private String message;
}
