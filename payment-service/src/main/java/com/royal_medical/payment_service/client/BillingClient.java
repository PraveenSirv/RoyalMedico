package com.royal_medical.payment_service.client;

import com.royal_medical.payment_service.dto.BillDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// This tells Feign to call through API-GATEWAY using service ID
@FeignClient(name = "API-GATEWAY", path = "/api/billing")
public interface BillingClient {

    @GetMapping("/bill/{id}")
    BillDTO getBillById(@PathVariable("id") Long id);

}
