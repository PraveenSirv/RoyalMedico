package com.royal_medical.billing_service.client;

import com.royal_medical.billing_service.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// This tells Feign to call through API-GATEWAY using service ID
@FeignClient(name = "API-GATEWAY", path = "/api/orders")
public interface OrderClient {

    @GetMapping("/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);

}
