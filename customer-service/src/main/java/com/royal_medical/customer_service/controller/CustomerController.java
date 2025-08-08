package com.royal_medical.customer_service.controller;

import com.royal_medical.customer_service.dto.CustomerDto;
import com.royal_medical.customer_service.dto.UserDTO;
import com.royal_medical.customer_service.model.Customer;
import com.royal_medical.customer_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Customer customer) {
        try {
            return new ResponseEntity<>(customerService.registerCustomer(customer),HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CustomerDto>> all() {
        try{
            return new ResponseEntity<>(customerService.getAllCustomers(),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        CustomerDto dto = customerService.getCustomerById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable String email) {
        System.out.println("Inside controller, email: " + email);
        CustomerDto dto = customerService.getCustomerByEmail(email); // Will throw exception if not found
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getCustomerByUsername(@PathVariable String username) {
        UserDTO dto = customerService.getCustomerByUsername(username);
        return ResponseEntity.ok(dto);
    }
}