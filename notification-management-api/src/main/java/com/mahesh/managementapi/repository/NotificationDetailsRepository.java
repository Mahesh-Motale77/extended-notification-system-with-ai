package com.mahesh.managementapi.repository;

import com.mahesh.managementapi.model.NotificationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDetailsRepository extends JpaRepository<NotificationDetails, Long> {
    Optional<NotificationDetails> findByOrderId(String orderId);

    List<NotificationDetails> findByNotificationStatus(NotificationDetails.NotificationStatus status);
}
