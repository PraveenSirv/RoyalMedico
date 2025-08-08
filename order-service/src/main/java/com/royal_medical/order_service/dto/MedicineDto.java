package com.royal_medical.order_service.dto;

import lombok.Data;

@Data
public class MedicineDto {
    private Long id;
    private String name;
    private String description;
    private int quantity;
    private double price;
    private boolean approved;

}
