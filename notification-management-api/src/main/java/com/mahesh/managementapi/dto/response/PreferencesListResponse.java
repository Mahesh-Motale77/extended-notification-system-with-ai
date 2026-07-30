package com.mahesh.managementapi.dto.response;

import com.mahesh.managementapi.dto.request.PreferenceRequest;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class PreferencesListResponse {
    private String statusCode;
    private String message;
    private String requestUUID;
    private List<PreferenceRequest> Data;
}
