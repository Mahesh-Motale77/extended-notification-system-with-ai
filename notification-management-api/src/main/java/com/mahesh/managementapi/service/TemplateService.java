package com.mahesh.managementapi.service;

import com.mahesh.managementapi.dto.request.TemplateRequest;
import com.mahesh.managementapi.dto.response.TemplateResponse;

public interface TemplateService {
    TemplateResponse saveTemplate(TemplateRequest templateRequest);

    TemplateResponse getTemplate(TemplateRequest templateRequest);
}
