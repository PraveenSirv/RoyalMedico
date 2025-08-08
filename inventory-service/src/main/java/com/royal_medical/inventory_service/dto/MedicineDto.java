package com.royal_medical.inventory_service.dto;

import jdk.jshell.Snippet;
import lombok.Builder;
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
