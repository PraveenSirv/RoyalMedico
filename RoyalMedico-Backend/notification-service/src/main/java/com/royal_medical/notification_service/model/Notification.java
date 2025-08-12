package com.royal_medical.notification_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private String type; // EMAIL, SMS, PUSH
    private String message;
    private LocalDateTime sentAt;
    private String status; // SENT, FAILED
}
