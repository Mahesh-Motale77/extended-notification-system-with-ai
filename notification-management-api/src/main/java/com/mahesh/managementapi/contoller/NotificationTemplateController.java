package com.mahesh.managementapi.contoller;

import com.mahesh.managementapi.dto.request.TemplateRequest;
import com.mahesh.managementapi.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Template API", description = "Manage notification templates with placeholders")
@RestController
@Data
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/template/api")
public class NotificationTemplateController {

    private final TemplateService templateService;

    @Operation(
            summary = "Register template",
            description = "Register email/SMS template with placeholders: {{channel}}, {{notificationType}}, {{bodyTemplate}}, {{subject}}"
    )
    @PostMapping(value = "/v1/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerTemplate(@RequestBody TemplateRequest templateRequest){
        log.info("Inside NotificationTemplateController -> registerTemplate() : TemplateRequest : {}",templateRequest);
        return ResponseEntity.ok(templateService.saveTemplate(templateRequest));
    }

    @Operation(
            summary = "Get template",
            description = "Fetch template by notification type and channel"
    )
    @PostMapping(value = "/v1/get", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTemplate(@RequestBody TemplateRequest templateRequest){
        log.info("Inside NotificationTemplateController -> getTemplate() : TemplateRequest : {}",templateRequest);
        return ResponseEntity.ok(templateService.getTemplate(templateRequest));
    }
}
