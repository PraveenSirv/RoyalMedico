package com.royal_medical.payment_service.service;

import com.royal_medical.payment_service.client.BillingClient;
import com.royal_medical.payment_service.dto.BillDTO;
import com.royal_medical.payment_service.dto.PaymentDTO;
import com.royal_medical.payment_service.exception.BillNotFoundException;
import com.royal_medical.payment_service.exception.ExternalServiceException;
import com.royal_medical.payment_service.exception.PaymentNotFoundException;
import com.royal_medical.payment_service.model.Payment;
import com.royal_medical.payment_service.repository.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final BillingClient billingClient;

    public PaymentDTO processPayment(PaymentDTO dto) {
        BillDTO bill;
        try {
            bill = billingClient.getBillById(dto.getBillId());
        }catch (FeignException.NotFound ex) {
            throw new BillNotFoundException("Bill not found with ID: " + dto.getBillId());
        } catch (FeignException e) {
            throw new ExternalServiceException("Billing Service is down.");
        } catch (Exception e) {
            throw new ExternalServiceException("Unexpected error while calling billing service for Bill ID: " + dto.getBillId());
        }

        if (bill == null || bill.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid bill details received for payment processing.");
        }

        Payment payment = new Payment();
        payment.setOrderId(bill.getOrderId());
        payment.setBillId(bill.getId());
        payment.setCustomerId(bill.getCustomerId());
        payment.setAmount(bill.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setStatus("PAID".equalsIgnoreCase(bill.getStatus()) ? "SUCCESS" : "PENDING");
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        return toDTO(payment);
    }


    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<PaymentDTO> getPaymentsByCustomer(Long customerId) {
        List<Payment> list = paymentRepository.findByCustomerId(customerId);
        if (list == null || list.isEmpty()) {
            throw new PaymentNotFoundException("No payments found for customer ID: " + customerId);
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }


    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));
        return toDTO(payment);
    }


    private PaymentDTO toDTO(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setOrderId(p.getOrderId());
        dto.setBillId(p.getBillId());
        dto.setCustomerId(p.getCustomerId());
        dto.setAmount(p.getAmount());
        dto.setMethod(p.getMethod());
        dto.setStatus(p.getStatus());
        dto.setPaymentDate(p.getPaymentDate());
        return dto;
    }
}
