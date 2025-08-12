package com.royal_medical.prescription_service.controller;

import com.royal_medical.prescription_service.dto.PrescriptionResponse;
import com.royal_medical.prescription_service.model.Prescription;
import com.royal_medical.prescription_service.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/upload")
    public ResponseEntity<Prescription> upload(@RequestParam("file") MultipartFile file, @RequestParam("customerId") Long customerId) throws IOException {
        Prescription prescription = prescriptionService.storeFile(file, customerId);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<PrescriptionResponse> getAllByCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByCustomer(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok(prescriptionService.deletePrescription(id));
    }
}