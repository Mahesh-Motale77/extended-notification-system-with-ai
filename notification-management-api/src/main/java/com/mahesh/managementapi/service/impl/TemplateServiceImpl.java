package com.mahesh.managementapi.service.impl;

import com.mahesh.managementapi.dto.request.TemplateRequest;
import com.mahesh.managementapi.dto.response.TemplateResponse;
import com.mahesh.managementapi.exception.NotificationException;
import com.mahesh.managementapi.model.NotificationTemplate;
import com.mahesh.managementapi.repository.NotificationTemplateRepository;
import com.mahesh.managementapi.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import static com.mahesh.managementapi.exception.ErrorCodes.TEMPLATE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    public TemplateResponse saveTemplate(TemplateRequest templateRequest){
        log.info("Inside TemplateServiceImpl --> saveTemplate() : TemplateRequest : {}", templateRequest);
        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                .channel(templateRequest.getChannel())
                .notificationType(templateRequest.getNotificationType())
                .subject(templateRequest.getSubject())
                .bodyTemplate(templateRequest.getBodyTemplate())
                .build();

        notificationTemplateRepository.save(notificationTemplate);

        return TemplateResponse.builder()
                .statusCode("200")
                .message("Template registered successfully!")
                .requestUUID(MDC.get("UUID"))
                .Data(templateRequest)
                .build();
    }

    @Override
    public TemplateResponse getTemplate(TemplateRequest templateRequest){
        log.info("Inside TemplateServiceImpl --> getTemplate() : TemplateRequest : {}", templateRequest);

        NotificationTemplate notificationTemplate = notificationTemplateRepository.findByNotificationTypeAndChannel(
                templateRequest.getNotificationType(), templateRequest.getChannel()
        ).orElseThrow(()-> new NotificationException(TEMPLATE_NOT_FOUND,"Template for found for given channel and notification type"));

        templateRequest.setBodyTemplate(notificationTemplate.getBodyTemplate());
        templateRequest.setSubject(notificationTemplate.getSubject());

        return TemplateResponse.builder()
                .statusCode("200")
                .message(null)
                .requestUUID(MDC.get("UUID"))
                .Data(templateRequest)
                .build();
    }
}
