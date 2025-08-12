package com.royal_medical.prescription_service.dto;

import com.royal_medical.prescription_service.model.Prescription;
import lombok.Data;

import java.util.List;


@Data
public class PrescriptionResponse {
    private String message;
    private List<Prescription> prescriptions;

    public PrescriptionResponse(String message, List<Prescription> prescriptions) {
        this.message = message;
        this.prescriptions = prescriptions;
    }

    // Getters and setters
}
