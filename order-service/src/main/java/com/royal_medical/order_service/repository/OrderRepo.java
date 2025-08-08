package com.royal_medical.order_service.repository;

import com.royal_medical.order_service.dto.OrderDTO;
import com.royal_medical.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface OrderRepo  extends JpaRepository<Order,Long> {

    List<Order> findByCustomerId(Long customerId);
}
