package com.royal_medical.inventory_service.controller;

import com.royal_medical.inventory_service.dto.MedicineDto;
import com.royal_medical.inventory_service.model.Medicine;
import com.royal_medical.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/medicine/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id) {
        MedicineDto dto = inventoryService.getMedicineById(id);
        return ResponseEntity.ok(dto); // 200 OK with medicine data
    }


    @PostMapping("/add")
    public ResponseEntity<MedicineDto> addMedicine(@RequestBody MedicineDto dto) {
        MedicineDto savedDto = inventoryService.addMedicine(dto);
        return ResponseEntity.ok(savedDto); // HTTP 201 Created
    }


    @GetMapping("/all")
    public ResponseEntity<List<MedicineDto>> getAllMedicines() {
        List<MedicineDto> medicines = inventoryService.getAllMedicines();
        return ResponseEntity.ok(medicines); // HTTP 200 OK
    }

    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        inventoryService.approveMedicine(id);
        return ResponseEntity.ok("Medicine with ID " + id + " approved successfully.");
    }

    @PutMapping("/update-quantity/{id}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long id, @RequestParam int quantity) {
        inventoryService.updateQuantity(id, quantity);
        return ResponseEntity.ok("Quantity of medicine with ID " + id + " updated to " + quantity + ".");
    }

    @PutMapping("/medicine/{id}/deduct")
    public ResponseEntity<String> deductMedicineQuantity(@PathVariable Long id, @RequestParam int quantity) {
        String message = inventoryService.deductMedicineQuantity(id, quantity);
        return ResponseEntity.ok(message);
    }


}
