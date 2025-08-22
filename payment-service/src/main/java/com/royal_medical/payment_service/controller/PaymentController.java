package com.royal_medical.payment_service.controller;

import com.royal_medical.payment_service.dto.PaymentDTO;
import com.royal_medical.payment_service.model.Payment;
import com.royal_medical.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentDTO> process(@RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(paymentService.processPayment(dto));
    }

    @GetMapping("/customer/{customerId}")
    public List<PaymentDTO> byCustomer(@PathVariable Long customerId) {
        return paymentService.getPaymentsByCustomer(customerId);
    }

    @GetMapping
    public List<PaymentDTO> all() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        PaymentDTO dto = paymentService.getPaymentById(id);
        return ResponseEntity.ok(dto);
    }


}

