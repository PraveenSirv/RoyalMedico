package com.royal_medical.billing_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long customerId;

    private double amount;
    private LocalDateTime billingDate;

    private String status; // e.g. PAID, UNPAID
}
