package com.royal_medical.order_service.client;

import com.royal_medical.order_service.dto.MedicineDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// This tells Feign to call through API-GATEWAY using service ID
@FeignClient(name = "API-GATEWAY", path = "/api/inventory")
public interface InventoryClient {

    @GetMapping("/medicine/{id}")
    MedicineDto getMedicineById(@PathVariable("id") Long id);

    @PutMapping("/medicine/{id}/deduct")
    void deductMedicineQuantity(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);


}
