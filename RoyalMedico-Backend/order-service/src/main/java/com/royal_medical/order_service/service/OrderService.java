package com.royal_medical.order_service.service;

import com.royal_medical.order_service.client.InventoryClient;
import com.royal_medical.order_service.dto.MedicineDto;
import com.royal_medical.order_service.dto.OrderDTO;
import com.royal_medical.order_service.exception.ExternalServiceException;
import com.royal_medical.order_service.exception.InvalidRequestException;
import com.royal_medical.order_service.exception.ResourceNotFoundException;
import com.royal_medical.order_service.model.Order;
import com.royal_medical.order_service.repository.OrderRepo;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepository;
    private final InventoryClient inventoryClient;

    public OrderDTO placeOrder(OrderDTO dto) {
        // 1. Fetch medicine
        MedicineDto medicine;
        try {
            medicine = inventoryClient.getMedicineById(dto.getMedicineId());
        } catch (FeignException.ServiceUnavailable e) {
            throw new ExternalServiceException("Inventory service is unavailable. Please try again later.");
        }
        catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Medicine with ID " + dto.getMedicineId() + " not found.");
        }

        // 2. Validate approval
        if (!medicine.isApproved()) {
            throw new InvalidRequestException("Medicine is not approved for ordering.");
        }

        int availableQty = medicine.getQuantity();
        int requestedQty = dto.getQuantity();

        // 3. Validate quantity
        if (availableQty < requestedQty) {
            throw new InvalidRequestException("Only " + availableQty + " units are available for medicine: " + medicine.getName());
        }

        // 4. Calculate total price
        double totalPrice = requestedQty * medicine.getPrice();

        // 5. Create and save order
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setMedicineId(dto.getMedicineId());
        order.setQuantity(requestedQty);
        order.setTotalPrice(totalPrice);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);

        // 6. Update inventory
        inventoryClient.deductMedicineQuantity(dto.getMedicineId(), requestedQty);

        // 7. Build and return DTO
        return mapToDto(order);
    }


    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID provided.");
        }

        List<Order> orders = orderRepository.findByCustomerId(customerId);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for customer ID: " + customerId);
        }
        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders available in the system.");
        }
        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToDto(order);
    }


    private OrderDTO mapToDto(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setMedicineId(order.getMedicineId());
        dto.setQuantity(order.getQuantity());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());
        return dto;
    }

}

