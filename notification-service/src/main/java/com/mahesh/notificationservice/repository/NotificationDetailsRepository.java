package com.mahesh.notificationservice.repository;

import com.mahesh.notificationservice.model.NotificationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationDetailsRepository extends JpaRepository<NotificationDetails, Long> {
    Optional<NotificationDetails> findByOrderId(String orderId);
}
