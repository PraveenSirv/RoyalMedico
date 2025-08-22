package com.royal_medical.order_service.controller;


import com.royal_medical.order_service.client.InventoryClient;
import com.royal_medical.order_service.dto.OrderDTO;
import com.royal_medical.order_service.model.Order;
import com.royal_medical.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderDTO dto) {
        return ResponseEntity.ok(orderService.placeOrder(dto));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> customerOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO dto = orderService.getOrderById(id);
        return ResponseEntity.ok(dto);
    }
}

