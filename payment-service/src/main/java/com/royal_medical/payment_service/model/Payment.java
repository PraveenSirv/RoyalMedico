package com.royal_medical.payment_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long billId;
    private Long customerId;

    private double amount;
    private LocalDateTime paymentDate;

    private String method; // e.g. CARD, UPI, CASH
    private String status; // e.g. SUCCESS, FAILED, PENDING
}

