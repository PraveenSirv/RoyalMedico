package com.royal_medical.admin_service.client;

import com.royal_medical.admin_service.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// This tells Feign to call through API-GATEWAY using service ID
@FeignClient(name = "API-GATEWAY", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/email/{email}")
    CustomerDto getCustomerByEmail(@PathVariable("email") String email);

    @GetMapping("/all")
    List<CustomerDto> all();


    @PutMapping("/update/{id}")
    void updateCustomer(@PathVariable("id") Long id, @RequestBody CustomerDto customerDto);

    @DeleteMapping("/delete/{id}")
    void deleteCustomer(@PathVariable("id") Long id);

}