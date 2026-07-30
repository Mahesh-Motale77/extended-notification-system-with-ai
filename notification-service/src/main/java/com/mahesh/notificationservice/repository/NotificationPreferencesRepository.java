package com.mahesh.notificationservice.repository;

import com.mahesh.notificationservice.model.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
    List<NotificationPreferences> findByUserId(String userId);
}
