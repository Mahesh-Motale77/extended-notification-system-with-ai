package com.mahesh.managementapi.dto.response;

import com.mahesh.managementapi.dto.request.TemplateRequest;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class TemplateResponse {
    private String statusCode;
    private String message;
    private String requestUUID;
    private TemplateRequest Data;
}
