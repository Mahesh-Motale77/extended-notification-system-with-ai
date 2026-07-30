package com.mahesh.managementapi.repository;

import com.mahesh.managementapi.model.NotificationDetails;
import com.mahesh.managementapi.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByNotificationTypeAndChannel(NotificationDetails.NotificationType notificationType, NotificationDetails.Channel channel);
}
