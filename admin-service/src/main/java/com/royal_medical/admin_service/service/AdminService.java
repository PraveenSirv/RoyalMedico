package com.royal_medical.admin_service.service;

import com.royal_medical.admin_service.client.CustomerClient;
import com.royal_medical.admin_service.dto.AdminDto;
import com.royal_medical.admin_service.dto.CustomerDto;
import com.royal_medical.admin_service.dto.UserDTO;
import com.royal_medical.admin_service.exception.AdminAlreadyExistsException;
import com.royal_medical.admin_service.exception.AdminNotFoundException;
import com.royal_medical.admin_service.exception.CustomerServiceException;
import com.royal_medical.admin_service.model.Admin;
import com.royal_medical.admin_service.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final CustomerClient customerClient;

    public List<AdminDto> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        if (admins.isEmpty()) {
            throw new AdminNotFoundException("No admin users found.");
        }
        return admins.stream().map(admin -> {
            AdminDto dto = new AdminDto();
            dto.setUsername(admin.getUsername());
            dto.setEmail(admin.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }

    public String registerAdmin(Admin admin) {
        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new AdminAlreadyExistsException("Admin with username '" + admin.getUsername() + "' already exists.");
        }
        if (adminRepository.findByUsername(admin.getEmail()).isPresent()) {
            throw new AdminAlreadyExistsException("Admin with Email '" + admin.getEmail() + "' already exists.");
        }
        adminRepository.save(admin);
        return "Success";
    }

    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        try {
            List<CustomerDto> customer = customerClient.all();
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            throw new CustomerServiceException("Failed to fetch customers from customer-service");
        }
    }

    public ResponseEntity<?> getCustomerByEmail(String email) {
        try {
            CustomerDto customer = customerClient.getCustomerByEmail(email);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            throw new CustomerServiceException("Customer not found for email: " + email);
        }
    }

    public UserDTO getByUsername(String username) {
        return adminRepository.findByUsername(username)
                .map(admin -> new UserDTO(admin.getUsername(), admin.getPassword(), "ADMIN"))
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with username: " + username));
    }

}
