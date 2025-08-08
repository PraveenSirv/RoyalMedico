package com.royal_medical.notification_service.service;

import com.royal_medical.notification_service.dto.NotificationDTO;
import com.royal_medical.notification_service.exception.InvalidRequestException;
import com.royal_medical.notification_service.exception.NotificationNotFoundException;
import com.royal_medical.notification_service.model.Notification;
import com.royal_medical.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDTO sendNotification(NotificationDTO dto) {
        if (dto.getCustomerId() == null || dto.getMessage() == null || dto.getType() == null) {
            throw new InvalidRequestException("Customer ID, message, and type are required.");
        }

        Notification notification = new Notification();
        notification.setCustomerId(dto.getCustomerId());
        notification.setType(dto.getType());
        notification.setMessage(dto.getMessage());
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }

    public List<NotificationDTO> getNotificationsByCustomer(Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
        if (notifications == null || notifications.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for customer ID: " + customerId);
        }

        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }


    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));
        return toDTO(notification);
    }

    public String deleteNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));
        notificationRepository.delete(notification);
        return "Notification deleted successfully.";
    }

    public List<NotificationDTO> getNotificationsByType(String type) {
        List<Notification> list = notificationRepository.findByType(type);
        if (list.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found with type: " + type);
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getNotificationsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Notification> list = notificationRepository.findBySentAtBetween(start, end);
        if (list.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found between the given date range.");
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<NotificationDTO> getAllPaginated(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::toDTO);
    }


    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setCustomerId(n.getCustomerId());
        dto.setType(n.getType());
        dto.setMessage(n.getMessage());
        dto.setSentAt(n.getSentAt());
        dto.setStatus(n.getStatus());
        return dto;
    }
}
