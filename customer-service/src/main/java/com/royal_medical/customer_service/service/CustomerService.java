package com.royal_medical.customer_service.service;

import com.royal_medical.customer_service.dto.CustomerDto;
import com.royal_medical.customer_service.dto.UserDTO;
import com.royal_medical.customer_service.exception.CustomerAlreadyExistsException;
import com.royal_medical.customer_service.exception.CustomerNotFoundException;
import com.royal_medical.customer_service.model.Customer;
import com.royal_medical.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;


    // Register + return DTO
    public CustomerDto registerCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()) != null) {
            throw new CustomerAlreadyExistsException(
                    "Customer with email '" + customer.getEmail() + "' already exists."
            );
        }
        Customer saved = customerRepository.save(customer);
        return mapToDto(saved);
    }


    // ---------- READ ----------
    public List<CustomerDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            throw new CustomerNotFoundException("No customers found.");
        }
        return customers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // Pagination + sorting
    public Page<CustomerDto> getAllCustomerspage(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToDto);
    }

    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found with email: " + email);
        }
        return mapToDto(customer);
    }

    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
        return mapToDto(customer);
    }

    public UserDTO getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .map(c -> new UserDTO(c.getUsername(), c.getPassword(), c.getRole()))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with username: " + username));
    }

    // Search customers (by username/email/address)
    public List<CustomerDto> searchCustomers(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return customerRepository.findAll().stream()
                .filter(c ->
                        (c.getUsername() != null && c.getUsername().toLowerCase().contains(lowerKeyword)) ||
                                (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerKeyword)) ||
                                (c.getAddress() != null && c.getAddress().toLowerCase().contains(lowerKeyword))
                )
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Filter by dynamic fields
    public List<CustomerDto> filterCustomers(Map<String, String> filters) {
        return customerRepository.findAll().stream()
                .filter(c -> filters.entrySet().stream().allMatch(entry -> {
                    String field = entry.getKey().toLowerCase();
                    String value = entry.getValue().toLowerCase();
                    switch (field) {
                        case "username":
                            return c.getUsername() != null && c.getUsername().toLowerCase().contains(value);
                        case "email":
                            return c.getEmail() != null && c.getEmail().toLowerCase().contains(value);
                        case "address":
                            return c.getAddress() != null && c.getAddress().toLowerCase().contains(value);
                        case "role":
                            return c.getRole() != null && c.getRole().toLowerCase().contains(value);
                        default:
                            return false;
                    }
                }))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ---------- DELETE ----------
    public String deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
        return "Deleted Successfully";
    }

    public String bulkDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("No IDs provided for bulk delete");
        }
        List<Long> notFound = ids.stream()
                .filter(id -> !customerRepository.existsById(id))
                .collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            throw new CustomerNotFoundException("Customers not found with IDs: " + notFound);
        }
        customerRepository.deleteAllById(ids);
        return "Deleted customers: " + ids;
    }


    // ---------- UPDATE ----------
    public CustomerDto updateCustomer(Long id, CustomerDto dto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        existingCustomer.setUsername(dto.getUsername());
        existingCustomer.setEmail(dto.getEmail());
        existingCustomer.setAddress(dto.getAddress());


        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return mapToDto(updatedCustomer);
    }

    public CustomerDto partialUpdateCustomer(Long id, Map<String, Object> updates) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        updates.forEach((key, value) -> {
            switch (key.toLowerCase()) {
                case "username":
                    existingCustomer.setUsername((String) value);
                    break;
                case "email":
                    existingCustomer.setEmail((String) value);
                    break;
                case "address":
                    existingCustomer.setAddress((String) value);
                    break;
                case "password":
                    existingCustomer.setPassword((String) value);
                    break;
                case "role":
                    existingCustomer.setRole((String) value);
                    break;
            }
        });

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return mapToDto(updatedCustomer);
    }

    // ---------- MAPPER ----------
    private CustomerDto mapToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        return dto;
    }
}
