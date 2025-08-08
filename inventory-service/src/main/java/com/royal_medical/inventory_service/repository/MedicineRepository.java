package com.royal_medical.inventory_service.repository;

import com.royal_medical.inventory_service.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine,Long> {

    boolean existsByName(String name);
}
