package com.royal_medical.payment_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private Long billId;
    private Long customerId;
    private double amount;
    private String method;
    private String status;
    private LocalDateTime paymentDate;
}
