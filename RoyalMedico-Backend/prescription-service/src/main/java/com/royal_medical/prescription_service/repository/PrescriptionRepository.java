package com.royal_medical.prescription_service.repository;

import com.royal_medical.prescription_service.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription,Long> {

    List<Prescription> findByCustomerId(Long customerId);

}
