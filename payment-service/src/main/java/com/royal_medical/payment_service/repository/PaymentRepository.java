package com.royal_medical.payment_service.repository;

import com.royal_medical.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    List<Payment> findByCustomerId(Long customerId);
}
