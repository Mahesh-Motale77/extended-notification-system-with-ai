package com.mahesh.managementapi.repository;

import com.mahesh.managementapi.model.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
    List<NotificationPreferences> findByUserId(String userId);
}
