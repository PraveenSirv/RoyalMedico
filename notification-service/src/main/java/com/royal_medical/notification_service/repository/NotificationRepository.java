package com.royal_medical.notification_service.repository;

import com.royal_medical.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByCustomerId(Long customerId);

    List<Notification> findByType(String type);

    List<Notification> findBySentAtBetween(LocalDateTime start, LocalDateTime end);
}
