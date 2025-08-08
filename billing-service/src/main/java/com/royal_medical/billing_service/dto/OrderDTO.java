package com.royal_medical.billing_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long medicineId;
    private int quantity;
    private double totalPrice;
    private LocalDateTime orderDate;
}
