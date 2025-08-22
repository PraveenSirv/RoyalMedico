package com.royal_medical.admin_service.service;

import com.royal_medical.admin_service.client.CustomerClient;
import com.royal_medical.admin_service.dto.AdminDto;
import com.royal_medical.admin_service.dto.CustomerDto;
import com.royal_medical.admin_service.dto.UserDto;
import com.royal_medical.admin_service.exception.AdminAlreadyExistsException;
import com.royal_medical.admin_service.exception.AdminNotFoundException;
import com.royal_medical.admin_service.exception.CustomerNotFoundException;
import com.royal_medical.admin_service.exception.CustomerServiceException;
import com.royal_medical.admin_service.model.Admin;
import com.royal_medical.admin_service.repository.AdminRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        }catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        } catch (Exception e) {
            throw new CustomerServiceException("Failed to fetch customers from customer-service");
        }
    }


    public ResponseEntity<CustomerDto> getCustomerByEmail(String email) {
        try {
            CustomerDto customer = customerClient.getCustomerByEmail(email);
            return ResponseEntity.ok(customer);
        }catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found for email: " + email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public UserDto getByUsername(String username) {
        return adminRepository.findByUsername(username)
                .map(admin -> new UserDto(
                        admin.getUsername(),
                        admin.getPassword(),
                        admin.getEmail(),
                        admin.getRole() // already set to "ADMIN" in your entity
                ))
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with username: " + username));
    }


    public ResponseEntity<String> updateAdmin(Long id, AdminDto adminDto) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));

        admin.setUsername(adminDto.getUsername());
        admin.setEmail(adminDto.getEmail());
        adminRepository.save(admin);

        return ResponseEntity.ok("Admin updated successfully");
    }


    public ResponseEntity<String> deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));

        adminRepository.delete(admin);
        return ResponseEntity.ok("Admin deleted successfully");
    }


    public ResponseEntity<String> updateCustomerByAdmin(Long id, CustomerDto customerDto) {
        try {
            customerClient.updateCustomer(id, customerDto); // This requires the customer-service to have an update endpoint
            return ResponseEntity.ok("Customer updated successfully");
        }catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        } catch (feign.FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity<String> deleteCustomerByAdmin(Long id) {
        try {
            customerClient.deleteCustomer(id); // Requires delete endpoint in customer-service
            return ResponseEntity.ok("Customer deleted successfully");
        }catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        }catch (feign.FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity<List<CustomerDto>> searchCustomers(String keyword) {
        try {
            List<CustomerDto> customers = customerClient.all();
            List<CustomerDto> filtered = customers.stream()
                    .filter(c -> c.getUsername().toLowerCase().contains(keyword.toLowerCase())
                            || c.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        }catch (Exception e) {
            throw new CustomerServiceException("Failed to search customers");
        }
    }


    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            long totalAdmins = adminRepository.count();
            long totalCustomers = customerClient.all().size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalCustomers", totalCustomers);

            return ResponseEntity.ok(stats);
        } catch (FeignException.ServiceUnavailable e) {
            throw new CustomerServiceException("Customer service is unavailable. Please try again later.");
        }catch (Exception e) {
            throw new CustomerServiceException("Failed to fetch dashboard stats");
        }
    }


}
