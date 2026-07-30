package com.mahesh.notificationservice.service.impl;

import com.mahesh.notificationservice.dto.EventRequest;
import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.repository.NotificationTemplateRepository;
import com.mahesh.notificationservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;

    public String buildMessage(EventRequest eventRequest, NotificationDetails.Channel channel){
        return notificationTemplateRepository.
                findByNotificationTypeAndChannel(NotificationDetails.NotificationType.valueOf(eventRequest.getOrderStatus()), channel)
                .map(template -> replacePlaceholders(template.getBodyTemplate(), eventRequest))
                .orElseGet(() -> buildDefaultMessage(eventRequest));
    }

    public String buildSubject(EventRequest eventRequest, NotificationDetails.Channel channel) {
        return notificationTemplateRepository
                .findByNotificationTypeAndChannel(NotificationDetails.NotificationType.valueOf(eventRequest.getOrderStatus()), channel)
                .map(template -> replacePlaceholders(template.getSubject(), eventRequest))
                .orElse("Notification - " + eventRequest.getOrderStatus());
    }

    private String buildDefaultMessage(EventRequest eventRequest) {
        return String.format(
                "Hi %s, your order %s status is %s.",
                eventRequest.getUserId(),
                eventRequest.getOrderId(),
                eventRequest.getOrderStatus()
        );
    }

    private String replacePlaceholders(String bodyTemplate, EventRequest eventRequest) {
        return bodyTemplate
                .replace("{{orderId}}",
                        String.valueOf(eventRequest.getOrderId()))
                .replace("{{userId}}",
                        String.valueOf(eventRequest.getUserId()))
                .replace("{{amount}}",
                        String.valueOf(eventRequest.getAmount()))
                .replace("{{items}}",
                        eventRequest.getItems())
                .replace("{{status}}",
                        eventRequest.getOrderStatus());
    }


}
