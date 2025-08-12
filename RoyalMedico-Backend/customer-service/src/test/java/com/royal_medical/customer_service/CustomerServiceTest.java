package com.royal_medical.customer_service;

import com.royal_medical.customer_service.dto.CustomerDto;
import com.royal_medical.customer_service.exception.CustomerNotFoundException;
import com.royal_medical.customer_service.repository.CustomerRepository;
import com.royal_medical.customer_service.service.CustomerService;
import com.royal_medical.customer_service.model.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    public CustomerServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCustomerById_Found() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsername("Praveen");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("Praveen", result.getUsername());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(99L);
        });

        assertEquals("Customer not found with ID: 99", exception.getMessage());
        verify(customerRepository, times(1)).findById(99L);
    }


}
