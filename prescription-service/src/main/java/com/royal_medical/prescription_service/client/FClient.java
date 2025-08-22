package com.royal_medical.prescription_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "API-GATEWAY", path = "/api/customers")
public interface FClient {

    @GetMapping("/id/{id}")
    ResponseEntity<?> getCustomer(@PathVariable Long id);

}
