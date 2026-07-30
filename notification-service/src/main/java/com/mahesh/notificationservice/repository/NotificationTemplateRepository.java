package com.mahesh.notificationservice.repository;

import com.mahesh.notificationservice.model.NotificationDetails;
import com.mahesh.notificationservice.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByNotificationTypeAndChannel(NotificationDetails.NotificationType notificationType, NotificationDetails.Channel channel);
}
