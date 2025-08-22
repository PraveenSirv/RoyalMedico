package com.royal_medical.notification_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long customerId;
    private String type;
    private String message;
    private LocalDateTime sentAt;
    private String status;
}
