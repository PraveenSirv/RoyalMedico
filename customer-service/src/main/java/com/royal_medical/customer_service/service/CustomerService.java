package com.royal_medical.customer_service.service;

import com.royal_medical.customer_service.dto.CustomerDto;
import com.royal_medical.customer_service.dto.UserDTO;
import com.royal_medical.customer_service.exception.CustomerAlreadyExistsException;
import com.royal_medical.customer_service.exception.CustomerNotFoundException;
import com.royal_medical.customer_service.model.Customer;
import com.royal_medical.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;


    public String registerCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()) != null) {
            throw new CustomerAlreadyExistsException("Customer with email '" + customer.getEmail() + "' already exists.");
        }
        customerRepository.save(customer);
        return "Successfully Saved Data to Database.";
    }


    public List<CustomerDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            throw new CustomerNotFoundException("No customers found.");
        }
        return customers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
                .map(customer -> new UserDTO(customer.getUsername(), customer.getPassword(), customer.getRole()))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with username: " + username));
    }

    private CustomerDto mapToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setPassword(customer.getPassword()); // Consider removing this if not necessary
        return dto;
    }


}
