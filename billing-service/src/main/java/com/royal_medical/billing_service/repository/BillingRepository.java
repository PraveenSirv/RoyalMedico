package com.royal_medical.billing_service.repository;

import com.royal_medical.billing_service.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface BillingRepository extends JpaRepository<Bill,Long> {

    List<Bill> findByCustomerId(Long customerId);
}
