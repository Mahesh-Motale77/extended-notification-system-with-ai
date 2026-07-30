package com.mahesh.managementapi.service.impl;

import com.mahesh.managementapi.dto.request.PreferenceRequest;
import com.mahesh.managementapi.dto.response.PreferencesListResponse;
import com.mahesh.managementapi.dto.response.PreferencesResponse;
import com.mahesh.managementapi.model.NotificationDetails;
import com.mahesh.managementapi.model.NotificationPreferences;
import com.mahesh.managementapi.repository.NotificationPreferencesRepository;
import com.mahesh.managementapi.service.PreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferencesServiceImpl implements PreferencesService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;

    public PreferencesResponse addPreferences(PreferenceRequest preferenceRequest){
        NotificationPreferences notificationPreferences = NotificationPreferences.builder()
                .userId(preferenceRequest.getUserId())
                .notificationType(NotificationDetails.NotificationType.valueOf(preferenceRequest.getNotificationType()))
                .channel(NotificationPreferences.Channel.valueOf(preferenceRequest.getChannel()))
                .isEnable(preferenceRequest.getIsEnable())
                .build();

        notificationPreferencesRepository.save(notificationPreferences);

        return PreferencesResponse.builder()
                .statusCode("200")
                .message("User preference added for userId : " + preferenceRequest.getUserId())
                .requestUUID(MDC.get("UUID"))
                .Data(preferenceRequest)
                .build();
    }

    public PreferencesListResponse getPreferences(String userId){
        List<NotificationPreferences> notificationPreferences = notificationPreferencesRepository.findByUserId(userId);

        List<PreferenceRequest> preferences = notificationPreferences.stream()
                .map(this::mapToNotificationResponse)
                .toList();

        return PreferencesListResponse.builder()
                .statusCode("200")
                .message(null)
                .requestUUID(MDC.get("UUID"))
                .Data(preferences)
                .build();
    }

    private PreferenceRequest mapToNotificationResponse(NotificationPreferences preferences) {
        return PreferenceRequest.builder()
                        .userId(preferences.getUserId())
                        .notificationType(String.valueOf(preferences.getNotificationType()))
                        .channel(String.valueOf(preferences.getChannel()))
                        .isEnable(preferences.getIsEnable())
                        .build();
    }
}
