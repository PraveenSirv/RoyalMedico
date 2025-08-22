package com.royal_medical.customer_service.repository;

import com.royal_medical.customer_service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    Optional<Customer> findByUsername(String username);

    Customer findByEmail(String email);
}

//
//@Query("SELECT c FROM Customer c WHERE " +
//        "LOWER(c.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//        "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//        "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//List<Customer> search(@Param("keyword") String keyword);
//
//
//@Query("SELECT c FROM Customer c WHERE " +
//        "(:address IS NULL OR LOWER(c.address) = LOWER(:address)) AND " +
//        "(:role IS NULL OR LOWER(c.role) = LOWER(:role))")
//List<Customer> filter(@Param("address") String address, @Param("role") String role);
