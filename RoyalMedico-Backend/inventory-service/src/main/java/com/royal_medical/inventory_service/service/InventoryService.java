package com.royal_medical.inventory_service.service;

import com.royal_medical.inventory_service.dto.MedicineDto;
import com.royal_medical.inventory_service.exception.DuplicateResourceException;
import com.royal_medical.inventory_service.exception.InsufficientStockException;
import com.royal_medical.inventory_service.exception.MedicineNotFoundException;
import com.royal_medical.inventory_service.model.Medicine;
import com.royal_medical.inventory_service.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final MedicineRepository repository;

    public MedicineDto addMedicine(MedicineDto dto) {
        if (repository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Medicine with name '" + dto.getName() + "' already exists.");
        }
        Medicine medicine = mapToEntity(dto);
        medicine.setApproved(false); // business rule
        Medicine saved = repository.save(medicine);
        return mapToDto(saved);
    }


    public List<MedicineDto> getAllMedicines() {
        List<Medicine> medicines = repository.findAll();
        if (medicines.isEmpty()) {
            throw new MedicineNotFoundException("No medicines available in inventory.");
        }
        return medicines.stream().map(this::mapToDto).collect(Collectors.toList());
    }


    public void approveMedicine(Long id) {
        Medicine med = repository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        med.setApproved(true);
        repository.save(med);
    }


    public void updateQuantity(Long id, int quantity) {
        Medicine med = repository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        med.setQuantity(quantity);
        repository.save(med);
    }


    public MedicineDto getMedicineById(Long id) {
        Medicine medicine = repository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));
        return mapToDto(medicine);
    }


    public String deductMedicineQuantity(Long id, int quantity) {
        Medicine medicine = repository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        if (medicine.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Only " + medicine.getQuantity() + " available.");
        }

        medicine.setQuantity(medicine.getQuantity() - quantity);
        repository.save(medicine);

        return "Stock updated successfully.";
    }

    private Medicine mapToEntity(MedicineDto dto) {
        Medicine medicine = new Medicine();
        medicine.setName(dto.getName());
        medicine.setDescription(dto.getDescription());
        medicine.setQuantity(dto.getQuantity());
        medicine.setPrice(dto.getPrice());
        return medicine;
    }


    private MedicineDto mapToDto(Medicine medicine) {
        MedicineDto dto = new MedicineDto();
        dto.setId(medicine.getId());
        dto.setName(medicine.getName());
        dto.setDescription(medicine.getDescription());
        dto.setQuantity(medicine.getQuantity());
        dto.setPrice(medicine.getPrice());
        dto.setApproved(medicine.isApproved()); // if needed
        return dto;
    }



}
