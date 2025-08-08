package com.royal_medical.billing_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BillDTO {
    private Long id;
    private Long orderId;
    private Long customerId;
    private double amount;
    private LocalDateTime billingDate;
    private String status;
}
