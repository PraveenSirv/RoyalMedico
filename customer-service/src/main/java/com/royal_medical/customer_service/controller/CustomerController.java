package com.royal_medical.customer_service.controller;

import com.royal_medical.customer_service.dto.CustomerDto;
import com.royal_medical.customer_service.dto.UserDTO;
import com.royal_medical.customer_service.exception.CustomerAlreadyExistsException;
import com.royal_medical.customer_service.exception.CustomerNotFoundException;
import com.royal_medical.customer_service.model.Customer;
import com.royal_medical.customer_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Checked
     * Register new customer and return the created object
     */
    @PostMapping("/register")
    public ResponseEntity<CustomerDto> register(@RequestBody Customer customer) {
        try {
            CustomerDto created = customerService.registerCustomer(customer);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (CustomerAlreadyExistsException | CustomerNotFoundException e) {
            throw e; // let GlobalExceptionHandler handle these
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Checked
     * Get all customers with pagination + sorting
     * Example: /api/customers/all?page=0&size=5&sort=username,asc
     */
    @GetMapping("/all/page")
    public ResponseEntity<Page<CustomerDto>> all(Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomerspage(pageable));
    }

    // Checked
    @GetMapping("/all")
    public ResponseEntity<List<CustomerDto>> all() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * Search by keyword (username, email, address)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDto>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(customerService.searchCustomers(keyword));
    }

    /**
     * Checked
     * Filter customers by field (example: /filter?address=NYC)
     */
    @GetMapping("/filter")
    public ResponseEntity<List<CustomerDto>> filterByField(@RequestParam Map<String, String> filters) {
        return ResponseEntity.ok(customerService.filterCustomers(filters));
    }

    //    Checked
    @GetMapping("/id/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    //    Checked
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    //    Checked
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getCustomerByUsername(@PathVariable String username) {
        return ResponseEntity.ok(customerService.getCustomerByUsername(username));
    }

    //    Checked
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }

    /**
     * Checked not working properly
     * Bulk delete customers
     */
    @PostMapping("/bulk-delete")
    public ResponseEntity<String> bulkDelete(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(customerService.bulkDelete(ids));
    }

    /**
     * Checked
     * Full update
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    /**
     * Checked
     * Partial update
     */
    @PatchMapping("/update/{id}")
    public ResponseEntity<CustomerDto> partialUpdateCustomer(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(customerService.partialUpdateCustomer(id, updates));
    }
}
