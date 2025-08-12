package com.royal_medical.billing_service.service;

import com.royal_medical.billing_service.client.OrderClient;
import com.royal_medical.billing_service.dto.BillDTO;
import com.royal_medical.billing_service.dto.OrderDTO;
import com.royal_medical.billing_service.exception.BillNotFoundException;
import com.royal_medical.billing_service.exception.ExternalServiceException;
import com.royal_medical.billing_service.model.Bill;
import com.royal_medical.billing_service.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final OrderClient orderClient;

    public BillDTO generateBill(BillDTO dto) {
        // Step 1: Validate and fetch order
        OrderDTO order;
        try {
            order = orderClient.getOrderById(dto.getOrderId());
        } catch (Exception e) {
            throw new ExternalServiceException("Unable to fetch order with ID: " + dto.getOrderId());
        }

        // Step 2: Build and save Bill entity
        Bill bill = new Bill();
        bill.setOrderId(order.getId());
        bill.setCustomerId(order.getCustomerId());
        bill.setAmount(order.getTotalPrice());
        bill.setBillingDate(LocalDateTime.now());
        bill.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        billingRepository.save(bill);

        // Step 3: Build and return DTO
        return mapToDTO(bill);
    }

    public List<BillDTO> getBillsByCustomer(Long customerId) {
        List<Bill> bills = billingRepository.findByCustomerId(customerId);
        if (bills == null || bills.isEmpty()) {
            throw new BillNotFoundException("No bills found for customer ID: " + customerId);
        }
        return bills.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<BillDTO> getAllBills() {
        List<Bill> bills = billingRepository.findAll();
        if (bills.isEmpty()) {
            throw new BillNotFoundException("No bills available in the system.");
        }
        return bills.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public BillDTO getBillById(Long id) {
        Bill bill = billingRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with ID: " + id));
        return mapToDTO(bill);
    }


    // Utility method to map entity to DTO
    private BillDTO mapToDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setOrderId(bill.getOrderId());
        dto.setCustomerId(bill.getCustomerId());
        dto.setAmount(bill.getAmount());
        dto.setBillingDate(bill.getBillingDate());
        dto.setStatus(bill.getStatus());
        return dto;
    }
}

