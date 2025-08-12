package com.royal_medical.billing_service.controller;

import com.royal_medical.billing_service.dto.BillDTO;
import com.royal_medical.billing_service.model.Bill;
import com.royal_medical.billing_service.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/generate")
    public ResponseEntity<BillDTO> generateBill(@RequestBody BillDTO dto) {
        BillDTO bill = billingService.generateBill(dto);
        return ResponseEntity.ok(bill);
    }


    @GetMapping("/customer/{customerId}")
    public List<BillDTO> getByCustomer(@PathVariable Long customerId) {
        return billingService.getBillsByCustomer(customerId);
    }

    @GetMapping
    public List<BillDTO> getAll() {
        return billingService.getAllBills();
    }

    @GetMapping("/bill/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id) {
        BillDTO dto = billingService.getBillById(id);
        return ResponseEntity.ok(dto);
    }
}

